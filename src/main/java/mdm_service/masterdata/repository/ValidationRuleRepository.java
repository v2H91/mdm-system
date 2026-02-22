package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {
    List<ValidationRule> findByEntityNameAndIsActiveTrue(String entityName);
    Optional<ValidationRule> findByEntityNameAndFieldNameAndIsActiveTrue(String entityName, String fieldName);
}
