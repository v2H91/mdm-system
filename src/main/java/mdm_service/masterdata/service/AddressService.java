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
    public void addAddress(AddressRequest request) {
        // 1. Validate Locations
        Location location = locationRepository.findByCode(request.locationCode()).orElseThrow();

        Address address = new Address();
        address.setHouseNumber(request.houseNumber());
        address.setStreet(request.street());
        address.setLocation(location);

        addressRepository.save(address);
    }
}
