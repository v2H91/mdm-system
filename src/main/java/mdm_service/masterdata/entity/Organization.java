package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mdm_service.masterdata.constant.Status;
import mdm_service.masterdata.listener.OrganizationListener;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@SQLDelete(sql = "UPDATE organizations SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@EntityListeners(OrganizationListener.class) // Đăng ký Listener
@Getter @Setter
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_code", unique = true, nullable = false)
    private String taxCode;

    private String legalName;
    private String shortName;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrgAddress> orgAddresses = new HashSet<>();

    @Column(name = "is_deleted") // Map tường minh với cột trong DB
    private boolean isDeleted = false;
}