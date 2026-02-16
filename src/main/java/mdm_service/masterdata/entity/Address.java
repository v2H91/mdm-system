package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.listener.AddressListener;

@Entity
@Table(name = "addresses")
@Getter @Setter
@EntityListeners(AddressListener.class)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String houseNumber;
    private String street;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code", referencedColumnName = "code")
    private Location ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "code")
    private Location province;

    private String fullAddress;

    @PrePersist
    @PreUpdate
    public void generateFullAddress() {
        String wardName = (ward != null) ? ward.getNameWithType() : "";
        String provinceName = (province != null) ? province.getNameWithType() : "";
        this.fullAddress = String.format("%s, %s, %s, %s",
                houseNumber, street, wardName, provinceName).replaceAll("^, |, $", "");
    }
}