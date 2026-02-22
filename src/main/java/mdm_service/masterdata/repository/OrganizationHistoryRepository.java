package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.OrganizationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationHistoryRepository extends JpaRepository<OrganizationHistory, Long> {
}
