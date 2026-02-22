package mdm_service.masterdata.controller;

import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.dto.LocationDTO;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.service.LocationImportService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.elasticsearch.core.sql.SqlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationImportService importService;
    private final JobRepository jobRepository;

    @PostMapping("/import")
    public ResponseEntity<String> importLocations(@RequestParam("file") MultipartFile file) {
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
            importService.importLocations(job.getId(), tempFile.toString());
            // 4. Trả về Job ID ngay lập tức
            return ResponseEntity.accepted().body("Job ID: " + job.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đọc file: " + e.getMessage());
        }
    }
}