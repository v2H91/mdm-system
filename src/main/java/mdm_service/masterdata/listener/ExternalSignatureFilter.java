package mdm_service.masterdata.listener;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.entity.ExternalClient;
import mdm_service.masterdata.helper.SignatureValidator;
import mdm_service.masterdata.repository.ExternalClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class ExternalSignatureFilter extends OncePerRequestFilter {
    @Autowired
    private SignatureValidator validator;
    @Autowired
    private ExternalClientRepository clientRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientId = request.getHeader("X-Client-ID");
        String signature = request.getHeader("X-Signature");
        String timestamp = request.getHeader("X-Timestamp");

        // Chỉ thực hiện xác thực nếu có đầy đủ các header cần thiết
        if (StringUtils.hasText(clientId) && StringUtils.hasText(signature) && StringUtils.hasText(timestamp)) {

            clientRepository.findByClientId(clientId)
                    .filter(ExternalClient::getIsActive)
                    .ifPresent(client -> {
                        try {
                            // Dữ liệu ký: kết hợp các trường để đảm bảo tính toàn vẹn
                            String dataToVerify = String.format("%s|%s", clientId, timestamp);

                            if (validator.verify(dataToVerify, signature, client.getPublicKey())) {
                                // Xác thực thành công: Gán quyền ROLE_EXTERNAL_SERVICE
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        client.getClientName(), // Sử dụng tên đối tác làm Principal
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_EXTERNAL_SERVICE"))
                                );

                                SecurityContextHolder.getContext().setAuthentication(auth);

                                // Đẩy thông tin vào MDC để trace log trên ES
                                log.info("Trace - Đối tác [{}] xác thực chữ ký thành công", client.getClientName());
                            } else {
                                log.warn("Trace - Chữ ký không khớp cho ClientID: {}", clientId);
                            }
                        } catch (Exception e) {
                            log.error("Trace - Lỗi kỹ thuật khi kiểm tra chữ ký đối tác {}: {}", clientId, e.getMessage());
                        }
                    });
        }

        filterChain.doFilter(request, response);
    }
}
