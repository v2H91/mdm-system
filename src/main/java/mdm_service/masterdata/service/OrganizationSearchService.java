package mdm_service.masterdata.service;

import lombok.RequiredArgsConstructor;
import mdm_service.masterdata.document.OrganizationDocument;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.repository.OrganizationSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationSearchService {

    private final OrganizationSearchRepository searchRepository;

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

        if (org.getOrgAddresses() != null) {
            List<OrganizationDocument.OrganizationAddressDocument> addrDocs = org.getOrgAddresses().stream()
                    .map(oa -> {
                        var ad = new OrganizationDocument.OrganizationAddressDocument();
                        ad.setFullAddress(oa.getAddress().getFullAddress());
                        ad.setAddressType(oa.getAddressType().name());
                        ad.setProvinceCode(oa.getAddress().getProvince().getCode());
                        ad.setWardCode(oa.getAddress().getWard().getCode());
                        return ad;
                    }).toList();
            doc.setAddresses(addrDocs);
        }
        return doc;
    }

    public void deleteFromIndex(Long id) {
        searchRepository.deleteById(id.toString());
    }
}