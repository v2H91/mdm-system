package mdm_service.masterdata.dto.request;

public record AddressRequest(
        String houseNumber,
        String street,
        String locationCode
) {}
