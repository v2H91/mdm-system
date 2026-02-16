package mdm_service.masterdata.dto.request;

public record AddressRequest(
        String houseNumber,
        String street,
        Long wardId,
        Long districtId,
        Long provinceId,
        String addressType // HEADQUARTER, BRANCH, WAREHOUSE
) {}
