package mdm_service.masterdata.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.helper.SpringContextHelper;
import mdm_service.masterdata.service.OrganizationSearchService;

@Slf4j
public class OrganizationListener {

    @PostPersist
    @PostUpdate
    public void onPostPersistOrUpdate(Organization org) {
        try {
            // Lấy SearchService từ Context
            OrganizationSearchService searchService = SpringContextHelper.getBean(OrganizationSearchService.class);

            // Map Entity sang Document và đẩy lên ES
            searchService.indexOrganization(org);
            log.info("Successfully synced Organization ID {} to Elasticsearch", org.getId());
        } catch (Exception e) {
            log.error("Failed to sync to Elasticsearch: {}", e.getMessage());
            // TODO: Có thể lưu vào một bảng 'sync_queue' để retry sau nếu ES chết
        }
    }

    @PostRemove
    public void onPostRemove(Organization org) {
        OrganizationSearchService searchService = SpringContextHelper.getBean(OrganizationSearchService.class);
        searchService.deleteFromIndex(org.getId());
    }
}
