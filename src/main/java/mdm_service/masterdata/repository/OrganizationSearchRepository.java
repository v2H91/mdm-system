package mdm_service.masterdata.repository;

import mdm_service.masterdata.document.OrganizationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrganizationSearchRepository extends ElasticsearchRepository<OrganizationDocument, String> {
}
