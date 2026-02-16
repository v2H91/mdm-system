package mdm_service.masterdata.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.dto.request.AddressRequest;
import mdm_service.masterdata.entity.Address;
import mdm_service.masterdata.entity.Location;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.exception.ResourceNotFoundException;
import mdm_service.masterdata.repository.AddressRepository;
import mdm_service.masterdata.repository.LocationRepository;
import mdm_service.masterdata.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final LocationRepository locationRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public void addAddressToOrg(AddressRequest request) {
        // 1. Validate Locations
        Location province = locationRepository.findById(request.provinceId()).orElseThrow();
        Location ward = locationRepository.findById(request.wardId()).orElseThrow();

        // 2. Normalization: Tạo full address tự động
        String fullAddress = String.format("%s, %s, %s, %s",
                request.houseNumber(), request.street(), ward.getName(),  province.getName());

        Address address = new Address();
        address.setHouseNumber(request.houseNumber());
        address.setStreet(request.street());
        address.setProvince(province);
        address.setWard(ward);
        address.setFullAddress(fullAddress);

        addressRepository.save(address);
        // TODO: Bắn Event đồng bộ địa chỉ sang các hệ thống Shipping/Logistics
    }
}
