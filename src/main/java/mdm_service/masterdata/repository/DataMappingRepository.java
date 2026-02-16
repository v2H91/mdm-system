package mdm_service.masterdata.repository;

import mdm_service.masterdata.entity.DataMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataMappingRepository extends JpaRepository<DataMapping, Long> {
    // Lấy config mapping cho một hệ thống cụ thể
    List<DataMapping> findAllBySourceSystemAndEntityTypeAndIsActiveTrue(String sourceSystem, String entityType);
}