package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "validation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false)
    private String entityName; // Ví dụ: 'ORGANIZATION', 'ADDRESS'

    @Column(name = "field_name", nullable = false)
    private String fieldName;  // Ví dụ: 'tax_code', 'email'

    @Column(name = "regex_pattern", nullable = false)
    private String regexPattern; // Ví dụ: '^[0-9]{10}$'

    @Column(name = "error_message")
    private String errorMessage; // Message trả về khi validate fail

    @Column(name = "is_active")
    private boolean isActive = true;

    // TODO: Thêm trường 'priority' nếu muốn áp dụng nhiều rule cho 1 field theo thứ tự
}
