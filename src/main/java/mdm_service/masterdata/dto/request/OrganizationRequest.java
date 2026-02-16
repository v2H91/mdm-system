package mdm_service.masterdata.dto.request;

import java.util.List;

public record OrganizationRequest(
        String taxCode,
        String legalName,
        String shortName,
        List<Long> addressIds
) {}
