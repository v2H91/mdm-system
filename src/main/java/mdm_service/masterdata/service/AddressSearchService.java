package mdm_service.masterdata.service;

import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.document.AddressDocument;
import mdm_service.masterdata.entity.Address;
import mdm_service.masterdata.repository.AddressSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressSearchService {

    private final AddressSearchRepository searchRepository;

    // 1. Đồng bộ đơn lẻ (Dùng cho JPA Listener)
    public void indexAddress(Address address) {
        AddressDocument doc = mapToDocument(address);
        searchRepository.save(doc);
    }

    // 2. Đồng bộ hàng loạt (Dùng cho Re-index)
    public void bulkIndexAddresses(List<Address> addresses) {
        List<AddressDocument> docs = addresses.stream()
                .map(this::mapToDocument)
                .toList();
        searchRepository.saveAll(docs);
    }

    public void deleteFromIndex(Long id) {
        searchRepository.deleteById(id.toString());
    }

    private AddressDocument mapToDocument(Address addr) {
        AddressDocument doc = new AddressDocument();
        doc.setId(addr.getId().toString());
        doc.setHouseNumber(addr.getHouseNumber());
        doc.setStreet(addr.getStreet());
        doc.setWardCode(addr.getWard() != null ? addr.getWard().getCode() : null);
        doc.setProvinceCode(addr.getProvince() != null ? addr.getProvince().getCode() : null);
        doc.setFullAddress(addr.getFullAddress());
        return doc;
    }
}