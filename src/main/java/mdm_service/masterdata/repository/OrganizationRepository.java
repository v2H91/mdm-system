package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.Organization;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    // Tìm kiếm chính xác theo mã số thuế
    Optional<Organization> findByTaxCode(String taxCode);

    // TODO: Triển khai Query nâng cao để lấy thông tin Organization kèm danh sách Address trong 1 lần query (Tránh lỗi N+1)
    @EntityGraph(attributePaths = {"addresses", "addresses.province", "addresses.district", "addresses.ward"})
    Optional<Organization> findWithFullDetailsById(Long id);

    // Kiểm tra tồn tại để chống trùng (Duplicate Detection)
    boolean existsByTaxCodeAndIsDeletedFalse(String taxCode);

    // Tìm kiếm theo tax_code (Master Data thường tìm theo mã này)
    @EntityGraph(attributePaths = {"addresses"})
    Optional<Organization> findByTaxCodeAndIsDeletedFalse(String taxCode);
}