package mdm_service.masterdata.repository;

import mdm_service.masterdata.document.AddressDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressSearchRepository extends ElasticsearchRepository<AddressDocument, String> {

    // Sử dụng Match Query với fuzziness AUTO để tìm kiếm mờ
    @Query("{\"match\": {\"fullAddress\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<AddressDocument> findByFullAddressFuzzy(String fullAddress);
}
