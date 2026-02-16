package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/addresses")
@Tag(name = "check Job API", description = "Quản lý Job proccesing")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;

    @GetMapping("/{jobId}")
    public ResponseEntity<BatchJob> getJobStatus(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
