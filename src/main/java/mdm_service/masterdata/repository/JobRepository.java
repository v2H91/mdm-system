package mdm_service.masterdata.repository;

import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.entity.BatchJob;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface JobRepository extends JpaRepository<BatchJob, String> {

    @Modifying
    @Query("UPDATE BatchJob j SET j.processedItems = :processed, j.totalItems = :total, j.status = :status WHERE j.id = :id")
    void updateJobProgress(@Param("id") String id,
                           @Param("processed") int processed,
                           @Param("total") int total,
                           @Param("status") JobStatus status);

    @Modifying
    @Query("UPDATE BatchJob j SET j.status = :status, j.endTime = :endTime WHERE j.id = :id")
    void updateJobStatus(@Param("id") String id,
                         @Param("status") JobStatus status,
                         @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Query("UPDATE BatchJob j SET j.status = :status, j.errorMessage = :error, j.endTime = :endTime WHERE j.id = :id")
    void updateJobError(@Param("id") String id,
                        @Param("status") JobStatus status,
                        @Param("error") String error,
                        @Param("endTime") LocalDateTime endTime);
}
