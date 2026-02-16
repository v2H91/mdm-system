package mdm_service.masterdata.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import mdm_service.masterdata.entity.Address;
import mdm_service.masterdata.helper.SpringContextHelper;
import mdm_service.masterdata.service.AddressSearchService;

public class AddressListener {

    @PostPersist
    @PostUpdate
    public void onPostPersistOrUpdate(Address addr) {
        try {
            AddressSearchService searchService = SpringContextHelper.getBean(AddressSearchService.class);
            searchService.indexAddress(addr);
        } catch (Exception e) {
            // Log error để không làm roll-back transaction chính
        }
    }

    @PostRemove
    public void onPostRemove(Address addr) {
        AddressSearchService searchService = SpringContextHelper.getBean(AddressSearchService.class);
        searchService.deleteFromIndex(addr.getId());
    }
}