package mdm_service.masterdata.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class OrgAddressId implements Serializable {
    private Long orgId;
    private Long addressId;
}
