package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.constant.JobStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_jobs")
@Getter
@Setter
public class BatchJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String jobName; // Ví dụ: REINDEX_ORGANIZATION

    @Enumerated(EnumType.STRING)
    private JobStatus status; // PENDING, RUNNING, COMPLETED, FAILED

    private int totalItems;
    private int processedItems;
    private String errorMessage;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

