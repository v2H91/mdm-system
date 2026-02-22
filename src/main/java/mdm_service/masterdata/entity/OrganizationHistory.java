package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.*;
import mdm_service.masterdata.constant.ActionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private ActionType actionType; // CREATE, UPDATE, APPROVE, REJECT

    @Column(columnDefinition = "JSON")
    private String changes;

    private String reason;
    private String performedBy;
    private LocalDateTime performedAt;
}
