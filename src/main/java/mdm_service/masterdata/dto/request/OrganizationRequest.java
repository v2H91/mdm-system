package mdm_service.masterdata.dto.request;

public record OrganizationRequest(
        String taxCode,
        String legalName,
        String shortName
) {}
