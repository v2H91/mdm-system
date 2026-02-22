package mdm_service.masterdata.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.constant.LocationType;

@Entity
@Table(name = "locations")
@Getter @Setter
public class Location {
    @Id
    private String code; // Ví dụ: "00004", "01"

    private String name; // Ví dụ: "Phường Ba Đình", "Thành phố Hà Nội"

    private LocationType type; // Ví dụ: "Phường", "Xã", "Thành phố", "Tỉnh"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code")
    @JsonIgnore // Ngăn việc trả về cả cây cha-con gây tràn bộ nhớ JSON
    private Location parent;

    private boolean isDeleted = false;
}