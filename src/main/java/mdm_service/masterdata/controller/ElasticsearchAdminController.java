package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.service.ReIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/elasticsearch")
@Tag(name = "Elasticsearch Admin", description = "Quản lý và tái lập chỉ mục dữ liệu")
@RequiredArgsConstructor
public class ElasticsearchAdminController {

    private final ReIndexService reIndexService;
    private final JobRepository jobRepository;

    @PostMapping("/reindex-organizations")
    @Operation(summary = "Xóa và Index lại toàn bộ Tổ chức")
    public ResponseEntity<String> reIndexOrganizations() {
        BatchJob job = new BatchJob();
        job.setJobName("REINDEX_ORGANIZATION");
        job.setStatus(JobStatus.PENDING);
        job = jobRepository.save(job);

        // Gọi async
        reIndexService.reIndexOrganizations(job.getId());

        return ResponseEntity.ok("Job started with ID: " + job.getId());
    }

    @PostMapping("/reindex-addresses")
    @Operation(summary = "Xóa và Index lại toàn bộ Địa chỉ")
    public ResponseEntity<String> reIndexLocations() {
        BatchJob job = new BatchJob();
        job.setJobName("REINDEX_ADDRESS");
        job.setStatus(JobStatus.PENDING);
        job = jobRepository.save(job);


        reIndexService.reIndexAddresses(job.getId());
        return ResponseEntity.ok("Job started with ID: " + job.getId());
    }
}
