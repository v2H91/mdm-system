package mdm_service.masterdata.service;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.constant.LocationType;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.entity.Location;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.repository.LocationRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LocationImportService {
    private final LocationRepository locationRepository;
    private final JobRepository jobRepository;
    @Async
    public void importLocations(String jobId, String tempFilePath) {
        File file = new File(tempFilePath);
        int processed = 0;
        int batchSize = 50;
        BatchJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.RUNNING);
        job.setStartTime(LocalDateTime.now());
        jobRepository.save(job);

        List<Location> batchList = new ArrayList<>();

        // 1. Sử dụng StreamingReader để chỉ giữ 100 dòng trong RAM tại một thời điểm
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)    // Số lượng dòng giữ trong bộ nhớ
                     .bufferSize(4096)     // Kích thước buffer đọc file
                     .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            // Lưu ý: StreamingReader không biết trước tổng số dòng, ta ước tính hoặc lấy từ meta

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua header

                // 2. Chuyển đổi Row thành Entity
                Location ward = convertRowToEntity(row);
                if (ward != null) {
                    batchList.add(ward);
                    processed++;
                }

                // 3. Thực hiện Batch Insert khi đủ số lượng
                if (batchList.size() >= batchSize) {
                    locationRepository.saveAll(batchList);
                    locationRepository.flush(); // Đẩy xuống DB
                    batchList.clear();
                }
            }

            // Lưu nốt số còn dư
            if (!batchList.isEmpty()) {
                locationRepository.saveAll(batchList);
            }

            job.setStatus(JobStatus.COMPLETED);

        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Lỗi tại dòng " + processed + ": " + e.getMessage());
        } finally {
            if (file.exists()) file.delete();
        }
    }

    private Location convertRowToEntity(Row row) {
        try {
            String wardCode = getCellValue(row.getCell(0));
            String wardName = getCellValue(row.getCell(1));
            String provinceCode = getCellValue(row.getCell(3));
            String provinceName = getCellValue(row.getCell(4));

            // Logic tìm/tạo Province
            Location province = locationRepository.findByCode(provinceCode)
                    .orElseGet(() -> {
                        Location p = new Location();
                        p.setCode(provinceCode);
                        p.setName(provinceName);
                        p.setType(LocationType.PROVINCE);
                        return locationRepository.saveAndFlush(p);
                    });

            Location ward = new Location();
            ward.setCode(wardCode);
            ward.setName(wardName);
            ward.setType(LocationType.WARD);
            ward.setParent(province);
            return ward;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getStringCellValue().trim();
    }
}