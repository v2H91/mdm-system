package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.constant.LocationType;

@Entity
@Table(name = "locations")
@Getter @Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;
    private String nameWithType;

    @Enumerated(EnumType.STRING)
    private LocationType type;

    @Column(name = "parent_code", length = 10)
    private String parentCode; // Khớp SQL

    @Column(columnDefinition = "TEXT")
    private String pathWithType;

    private boolean isDeleted = false;
}