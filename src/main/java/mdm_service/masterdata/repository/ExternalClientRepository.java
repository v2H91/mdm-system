package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.ExternalClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExternalClientRepository extends JpaRepository<ExternalClient, Long> {

    /**
     * Tìm kiếm đối tác theo API Key (clientId)
     * Thường dùng trong Filter để lấy Public Key xác thực chữ ký
     */
    Optional<ExternalClient> findByClientId(String clientId);

    /**
     * Kiểm tra đối tác có đang hoạt động hay không
     */
    boolean existsByClientIdAndIsActiveTrue(String clientId);
}