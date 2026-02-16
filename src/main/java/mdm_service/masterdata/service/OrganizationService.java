package mdm_service.masterdata.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.constant.Status;
import mdm_service.masterdata.dto.request.OrganizationRequest;
import mdm_service.masterdata.dto.response.OrganizationResponse;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.exception.BusinessException;
import mdm_service.masterdata.exception.ResourceNotFoundException;
import mdm_service.masterdata.repository.OrganizationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ValidationService validationService; // Service tự viết để check Regex
    private final ApplicationEventPublisher eventPublisher; // Dùng cho Event-driven

    @Transactional
    @CacheEvict(value = "organizations", allEntries = true)
    public OrganizationResponse create(OrganizationRequest request) {
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
        Organization org = organizationRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        return mapToResponse(org);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        // 6. Global/Local Mapping
        // TODO: Query bảng data_mappings để lấy config hiển thị theo field
        Map<String, String> mappings = Map.of(
                "legalName", "Company_Name",
                "taxCode", "Tax_ID"
        );

        return new OrganizationResponse(
                org.getId(),
                org.getTaxCode(),
                org.getLegalName(),
                org.getStatus().name(),
                mappings
        );
    }

    public void delete(Long id) {
    }
}
