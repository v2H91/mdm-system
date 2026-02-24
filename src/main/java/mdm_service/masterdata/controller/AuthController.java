package mdm_service.masterdata.controller;

import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.dto.request.LoginRequest;
import mdm_service.masterdata.dto.response.JwtResponse;
import mdm_service.masterdata.helper.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

   private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Xác thực thông tin từ Database
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // 2. Set thông tin vào Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Tạo Token
            String jwt = tokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản hoặc mật khẩu không đúng");
        }
    }
}
