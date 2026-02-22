package mdm_service.masterdata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitorjbl.xlsx.StreamingReader;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.constant.ActionType;
import mdm_service.masterdata.constant.JobStatus;
import mdm_service.masterdata.constant.Status;
import mdm_service.masterdata.dto.request.OrganizationRequest;
import mdm_service.masterdata.dto.response.OrganizationResponse;
import mdm_service.masterdata.entity.BatchJob;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.entity.OrganizationHistory;
import mdm_service.masterdata.exception.BusinessException;
import mdm_service.masterdata.exception.ResourceNotFoundException;
import mdm_service.masterdata.repository.JobRepository;
import mdm_service.masterdata.repository.OrganizationRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ValidationService validationService; // Service tự viết để check Regex
    private final ApplicationEventPublisher eventPublisher; // Dùng cho Event-driven
    private final JobRepository jobRepository;
    private final OrganizationSearchService searchService;
    private final OrganizationHistoryService historyService;
    private final ObjectMapper objectMapper;

    @Transactional
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationResponse create(OrganizationRequest request) {
        validationService.validate("organization", request, null);
        log.info("Creating organization with tax code: {}", request.taxCode());

        // 1. Data Validation & Normalization
        // TODO: Chỗ này gọi ValidationService để load Regex từ DB bảng validation_rules
        validationService.validate("ORGANIZATION", "tax_code", request.taxCode());
        validationService.validate("ORGANIZATION", "legal_name", request.legalName());

        // 2. Duplicate Detection
        // TODO: Triển khai thuật toán Levenshtein Distance để check trùng tên tương đối
        if (organizationRepository.findByTaxCode(request.taxCode()).isPresent()) {
            throw new BusinessException("Mã số thuế đã tồn tại trong hệ thống Master Data");
        }

        // 3. Mapping & Saving
        Organization org = new Organization();
        org.setTaxCode(request.taxCode().trim()); // Normalization
        org.setLegalName(request.legalName().toUpperCase()); // Normalization
        org.setShortName(request.shortName());
        org.setStatus(Status.PENDING); // Mặc định chờ duyệt (Workflow)

        Organization savedOrg = organizationRepository.save(org);

        // 4. Audit Trail
        // TODO: Lưu giá trị cũ (null) và giá trị mới vào bảng audit_logs dạng JSON

        // 5. Event-driven (Giả lập)
        // TODO: Bắn event để các hệ thống con đồng bộ dữ liệu
        //eventPublisher.publishEvent(new OrganizationCreatedEvent(this, savedOrg.getId()));

        return mapToResponse(savedOrg);
    }

    @Cacheable(value = "organizations", key = "#id")
    public OrganizationResponse getById(Long id) {
        Organization org = organizationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        return mapToResponse(org);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        // 6. Global/Local Mapping
        // TODO: Query bảng data_mappings để lấy config hiển thị theo field
        Map<String, String> mappings = Map.of("legalName", "Company_Name", "taxCode", "Tax_ID");

        return new OrganizationResponse(org.getId(), org.getTaxCode(), org.getLegalName(), org.getStatus().name(), mappings);
    }

    public void delete(Long id) {
    }


    @Async
    public StreamingResponseBody executeExportJob(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=organizations_export.xlsx");

        return outputStream -> {
            // Sử dụng SXSSF để ghi dữ liệu theo luồng (Streaming)
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
                Sheet sheet = workbook.createSheet("Organizations");

                // Tạo Header
                this.createHeader(sheet);

                // 2. Lấy dữ liệu theo từng trang và ghi vào sheet
                int rowNum = 1;
                int pageSize = 1000;
                Pageable pageable = PageRequest.of(0, pageSize);
                Page<Organization> page;

                do {
                    page = organizationRepository.findAll(pageable);
                    for (Organization org : page.getContent()) {
                        Row row = sheet.createRow(rowNum++);
                        this.fillRow(row, org);
                    }
                    // Flush bộ nhớ tạm xuống đĩa để giải phóng RAM
                    ((SXSSFSheet) sheet).flushRows(100);
                    pageable = pageable.next();
                } while (page.hasNext());

                // 3. Ghi dữ liệu vào OutputStream của HTTP Response
                workbook.write(outputStream);
                workbook.dispose(); // Xóa các file tạm của SXSSF
            } catch (Exception e) {
                log.error("Lỗi khi export file trực tiếp: ", e);
            }
        };
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Mã Số Thuế (taxCode)");
        header.createCell(1).setCellValue("Tên Pháp Lý (legalName)");
        header.createCell(2).setCellValue("Tên Viết Tắt (shortName)");
        header.createCell(3).setCellValue("Mã Xã/Phường (locationCode)");
    }

    private void fillRow(Row row, Organization org) {
        row.createCell(0).setCellValue(org.getTaxCode());
        row.createCell(1).setCellValue(org.getLegalName());
        row.createCell(2).setCellValue(org.getShortName());
    }

    @Async
    public void executeImportJob(String jobId, String tempPath) {
        BatchJob job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(JobStatus.RUNNING);
        job.setStartTime(LocalDateTime.now());
        jobRepository.save(job);
        List<Organization> batchList = new ArrayList<>();

        try (InputStream is = new FileInputStream(tempPath);
             Workbook workbook = StreamingReader.builder().rowCacheSize(100).open(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Organization org = mapRowToOrg(row);
                batchList.add(org);

                if (batchList.size() >= 50) {
                    organizationRepository.saveAll(batchList);
                    batchList.clear();
                }
            }
            if (!batchList.isEmpty()) organizationRepository.saveAll(batchList);
            job.setStatus(JobStatus.COMPLETED);
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);
            new File(tempPath).delete();

        }
    }

    private Organization mapRowToOrg(Row row) {
        Organization org = new Organization();
        org.setTaxCode(getCellValue(row.getCell(0)));
        org.setLegalName(getCellValue(row.getCell(1)));
        org.setShortName(getCellValue(row.getCell(2)));
        return org;
    }

    private String getCellValue(Cell cell) {
        return cell == null ? "" : cell.getStringCellValue();
    }

    @Transactional
    public void approve(Long id, String userEmail) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Org"));

        if (org.getStatus() == Status.APPROVED) return;

        // 1. Cập nhật trạng thái trong MySQL
        org.setStatus(Status.APPROVED);
        org.setApprovedBy(userEmail);
        org.setApprovedAt(LocalDateTime.now());
        organizationRepository.save(org);


        // 2. Đồng bộ sang Elasticsearch
        searchService.indexOrganization(org);

        log.info("Trace - Org ID: {} đã được duyệt bởi {}", id, userEmail);
    }

    public void approveBulk(List<Long> ids, String userEmail) {
        for (Long id : ids) {
            try {
                approve(id, userEmail);
            } catch (Exception e) {
                log.error("Lỗi duyệt Org ID {}: {}", id, e.getMessage());
            }
        }
    }

    @Transactional
    public void reject(Long id, String reason, String userEmail) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Org"));

        // 1. Cập nhật trạng thái và lý do trong MySQL
        org.setStatus(Status.REJECTED);
        org.setRejectedReason(reason);
        org.setRejectedBy(userEmail);
        org.setRejectedAt(LocalDateTime.now());
        organizationRepository.save(org);

        // 2. Ghi lại lịch sử
        OrganizationHistory history = OrganizationHistory.builder()
                .organization(org)
                .actionType(ActionType.REJECT)
                .reason(reason)
                .performedBy(userEmail)
                .performedAt(LocalDateTime.now())
                .build();
        historyService.save(history);

        searchService.indexOrganization(org);
        log.info("Trace - Org ID: {} bị từ chối bởi {}. Lý do: {}", id, userEmail, reason);
    }

    @Transactional
    public void requestUpdate(Long id, OrganizationRequest updateDTO) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Org không tồn tại"));

        validationService.validate("organization", updateDTO, org);

        // Chuyển DTO thành JSON để lưu vào hàng đợi duyệt
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonUpdate = mapper.writeValueAsString(updateDTO);
            org.setPendingData(jsonUpdate);
            org.setStatus(Status.PENDING); // Đưa về trạng thái chờ duyệt
            org.setIsEditing(true);
            organizationRepository.save(org);

            log.info("Trace - Request update cho Org ID: {}. Dữ liệu cũ vẫn được giữ nguyên trên ES.", id);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi xử lý dữ liệu nháp");
        }
    }

    @Transactional
    public void approveUpdate(Long id, String approvedBy) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Org không tồn tại"));

        if (org.getIsEditing() && org.getPendingData() != null) {
            try {
                // 1. Đọc dữ liệu chờ duyệt (Draft)
                OrganizationRequest draftDto = objectMapper.readValue(org.getPendingData(), OrganizationRequest.class);

                // 2. Ghi lại lịch sử thay đổi TRƯỚC khi ghi đè dữ liệu mới
                historyService.recordUpdateHistory(org, draftDto, approvedBy);

                // 3. Cập nhật dữ liệu chính (Golden Record)
                org.setLegalName(draftDto.legalName());
                org.setShortName(draftDto.shortName());
                org.setTaxCode(draftDto.taxCode());

                // 4. Reset trạng thái staging
                org.setIsEditing(false);
                org.setPendingData(null);
                org.setStatus(Status.APPROVED);
                org.setApprovedBy(approvedBy);
                org.setApprovedAt(LocalDateTime.now());

                organizationRepository.save(org);

                // 5. Đồng bộ ES
                searchService.indexOrganization(org);

            } catch (IOException e) {
                log.error("Lỗi duyệt cập nhật: {}", e.getMessage());
            }
        }
    }
}

