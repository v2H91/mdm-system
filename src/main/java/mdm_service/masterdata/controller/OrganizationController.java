package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.document.OrgHistoryDocument;
import mdm_service.masterdata.document.OrganizationDocument;
import mdm_service.masterdata.dto.request.OrganizationRequest;
import mdm_service.masterdata.dto.response.OrganizationResponse;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.service.OrganizationHistoryService;
import mdm_service.masterdata.service.OrganizationSearchService;
import mdm_service.masterdata.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization Management", description = "Quản lý dữ liệu Master cho Tổ chức")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;
    private final OrganizationSearchService searchService;
    private final JobRepository jobRepository;
    private final OrganizationHistoryService historyService;


    @PostMapping
    @Operation(summary = "Tạo mới tổ chức", description = "Hệ thống sẽ validate dựa trên regex trong DB")
    public ResponseEntity<OrganizationResponse> create(@RequestBody OrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orgService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orgService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganizationDocument>> search(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(searchService.search(query));
    }

    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportDirect(HttpServletResponse response) {
        StreamingResponseBody stream = orgService.executeExportJob(response);

        return ResponseEntity.ok(stream);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("org_import_", ".xlsx");
        file.transferTo(tempFile.toFile());

        BatchJob job = new BatchJob();
        job.setJobName("IMPORT_ORG_EXCEL");
        job.setStatus(JobStatus.PENDING);
        job = jobRepository.save(job);
        orgService.executeImportJob(job.getId(), tempFile.toString());

        return ResponseEntity.accepted().body("Job ID: " + job.getId());
    }

    // Phê duyệt 1 bản ghi
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        // Trong thực tế sẽ lấy email từ SecurityContext
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        orgService.approve(id, currentUser);
        return ResponseEntity.ok().build();
    }

    // Phê duyệt hàng loạt qua Job
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    @PostMapping("/bulk-approve")
    public ResponseEntity<String> bulkApprove(@RequestBody List<Long> ids) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        orgService.approveBulk(ids, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestBody String reason) {
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        orgService.reject(id, reason, currentUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/request-edit")
    public ResponseEntity<String> requestEdit(@PathVariable Long id, @RequestBody OrganizationRequest dto) {
        orgService.requestUpdate(id, dto);
        return ResponseEntity.ok("Yêu cầu chỉnh sửa đã được gửi, đang chờ phê duyệt.");
    }

    @PostMapping("/{id}/approve-edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER')")
    public ResponseEntity<Void> approveEdit(@PathVariable Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        orgService.approveUpdate(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<OrgHistoryDocument>> getOrgTimeline(@PathVariable("id") Long orgId) {
        return ResponseEntity.ok(historyService.getTimeline(orgId));
    }
}
