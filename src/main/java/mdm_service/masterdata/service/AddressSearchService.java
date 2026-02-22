package mdm_service.masterdata.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.document.AddressDocument;
import mdm_service.masterdata.entity.Address;
import mdm_service.masterdata.entity.Location;
import mdm_service.masterdata.repository.AddressSearchRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressSearchService {

    private final AddressSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
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

        // Map các trường cơ bản
        doc.setId(addr.getId().toString());
        doc.setHouseNumber(addr.getHouseNumber());
        doc.setStreet(addr.getStreet());
        doc.setFullAddress(addr.getFullAddress());

        // Xử lý lấy mã từ cấu trúc Location phân cấp
        if (addr.getLocation() != null) {
            // Cấp thấp nhất (Xã/Phường) chính là location của Address
            doc.setWardCode(addr.getLocation().getCode());

            // Tìm cấp Tỉnh/Thành phố bằng cách duyệt ngược lên cha
            Location current = addr.getLocation();
            while (current.getParent() != null) {
                current = current.getParent();
            }
            // Sau khi loop xong, current sẽ là node gốc (Tỉnh/Thành phố)
            doc.setProvinceCode(current.getCode());
        }

        return doc;
    }

    /**
     * Tìm kiếm địa chỉ mờ (Fuzzy Search) theo từ khóa
     * @param query Từ khóa tìm kiếm (ví dụ: "Số 10 Tràng Thi")
     * @return Danh sách AddressDocument khớp nhất
     */
    public List<AddressDocument> search(String query) {
        // Xây dựng câu truy vấn Native
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("fullAddress") // Trường tìm kiếm chính
                                .query(query)
                                .operator(Operator.And)
                                .fuzziness("AUTO")    // Cho phép sai lệch ký tự tự động
                                .analyzer("vi_analyzer") // Dùng analyzer ICU đã cấu hình
                        )
                )
                .withMaxResults(20) // Giới hạn kết quả trả về để tối ưu hiệu năng
                .build();

        // Thực thi tìm kiếm
        SearchHits<AddressDocument> searchHits = elasticsearchOperations.search(nativeQuery, AddressDocument.class);

        // Chuyển đổi SearchHits sang List nội dung
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}