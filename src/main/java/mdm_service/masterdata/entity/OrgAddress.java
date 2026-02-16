package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.constant.AddressType;

@Entity
@Table(name = "org_addresses")
@Getter
@Setter
public class OrgAddress {

    @EmbeddedId
    private OrgAddressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orgId")
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addressId")
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private AddressType addressType; // HEADQUARTER, BRANCH, WAREHOUSE
}


