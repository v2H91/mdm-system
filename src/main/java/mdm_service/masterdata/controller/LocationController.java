package mdm_service.masterdata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.entity.Location;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.service.LocationService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final JobRepository jobRepository;

    @Operation(summary = "Import địa chỉ từ file Excel")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importLocations(
            @Parameter(description = "File Excel chứa danh sách địa chỉ (.xlsx)")
            @RequestPart("file") MultipartFile file) {
        try {
            // 1. Tạo file tạm trên đĩa để StreamingReader có thể đọc từ luồng file
            Path tempFile = Files.createTempFile("import_loc_", ".xlsx");
            file.transferTo(tempFile.toFile());

            // 2. Tạo bản ghi Job
            BatchJob job = new BatchJob();
            job.setJobName("IMPORT_LOCATION_EXCEL");
            job.setStatus(JobStatus.PENDING);
            job = jobRepository.save(job);

            // 3. Gọi Service xử lý ngầm
            locationService.importLocations(job.getId(), tempFile.toString());
            // 4. Trả về Job ID ngay lập tức
            return ResponseEntity.accepted().body("Job ID: " + job.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đọc file: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER', 'VIEWER')")
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Location> locations = locationService.getAllLocations(page, size);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'APPROVER', 'VIEWER')")
    public ResponseEntity<List<Location>> searchLocation(@RequestParam String name) {
        List<Location> results = locationService.searchByName(name);
        return ResponseEntity.ok(results);
    }
}