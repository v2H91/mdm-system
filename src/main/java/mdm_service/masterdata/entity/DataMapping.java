package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "data_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem; // Ví dụ: 'SAP_ERP', 'CRM_SALESFORCE'

    @Column(name = "entity_type", nullable = false)
    private String entityType;   // Ví dụ: 'ORGANIZATION'

    @Column(name = "local_field", nullable = false)
    private String localField;   // Tên field ở hệ thống con (e.g., 'VAT_Number')

    @Column(name = "global_field", nullable = false)
    private String globalField;  // Tên field chuẩn ở MDM (e.g., 'taxCode')

    @Column(name = "is_active")
    private boolean isActive = true;

    // TODO: Thêm trường 'transform_script' (JavaScript hoặc SpEL) để format lại dữ liệu khi mapping
}