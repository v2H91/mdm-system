package mdm_service.masterdata.dto.response;

import java.util.Map;

public record OrganizationResponse(
        Long id,
        String taxCode,
        String legalName,
        String status,
        Map<String, String> mappings // Dùng cho tính năng Global/Local Mapping bạn muốn
) {}
