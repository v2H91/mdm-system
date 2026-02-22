package mdm_service.masterdata.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.document.OrganizationDocument;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.repository.OrganizationSearchRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationSearchService {

    private final OrganizationSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void bulkIndexOrganizations(List<Organization> organizations) {
        List<OrganizationDocument> docs = organizations.stream()
                .map(this::convertToDocument)
                .toList();

        searchRepository.saveAll(docs); // Spring Data ES sẽ tự thực hiện Bulk Request
    }

    public void indexOrganization(Organization org) {
        OrganizationDocument doc = this.convertToDocument(org);

        searchRepository.save(doc);
    }

    private OrganizationDocument convertToDocument(Organization org) {
        OrganizationDocument doc = new OrganizationDocument();
        doc.setId(org.getId().toString());
        doc.setTaxCode(org.getTaxCode());
        doc.setLegalName(org.getLegalName());
        doc.setShortName(org.getShortName());
        doc.setStatus(org.getStatus().name());
        doc.setRejectedReason(org.getRejectedReason());
//        if (org.getOrgAddresses() != null) {
//            List<OrganizationDocument.OrganizationAddressDocument> addrDocs = org.getOrgAddresses().stream()
//                    .map(oa -> {
//                        var ad = new OrganizationDocument.OrganizationAddressDocument();
//                        ad.setFullAddress(oa.getAddress().getFullAddress());
//                        ad.setAddressType(oa.getAddressType().name());
//                        ad.setProvinceCode(oa.getAddress().getLocation().getCode());
//                        return ad;
//                    }).toList();
//            doc.setAddresses(addrDocs);
//        }
        return doc;
    }

    public void deleteFromIndex(Long id) {
        searchRepository.deleteById(id.toString());
    }

    public List<OrganizationDocument> search(String keyword) {
        // Tạo query tìm kiếm mờ trên cả tên pháp lý, tên viết tắt và địa chỉ
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("legalName", "shortName", "addresses.fullAddress")
                                .query(keyword)
                                .operator(Operator.And)
                                .fuzziness("AUTO") // Tự động xử lý sai lệch ký tự
                                .analyzer("vi_analyzer") // Sử dụng analyzer tiếng Việt đã cấu hình
                        )
                )
                .build();

        SearchHits<OrganizationDocument> searchHits = elasticsearchOperations.search(query, OrganizationDocument.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
    }
}