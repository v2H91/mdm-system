package mdm_service.masterdata.service;

import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.constant.LocationType;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.entity.Location;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.repository.LocationRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LocationService {
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

        try (Workbook workbook = WorkbookFactory.create(file))  {
            Sheet sheet = workbook.getSheetAt(0);
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
                    locationRepository.flush();
                    batchList.clear();
                }
            }
            job.setStatus(JobStatus.COMPLETED);
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Lỗi tại dòng " + processed + ": " + e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);
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

    public Page<Location> getAllLocations(int page, int size) {
        // Tạo đối tượng Pageable (trang bắt đầu từ 0)
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return locationRepository.findAll(pageable);
    }

    public List<Location> searchByName(String name) {
        // Sử dụng JPA để truy vấn trực tiếp vào MySQL
        return locationRepository.findByNameContainingIgnoreCase(name);
    }
}