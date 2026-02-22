package mdm_service.masterdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "validation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName; // Ví dụ: 'organization', 'address'

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;  // Ví dụ: 'taxCode', 'email'

    @Column(name = "regex_pattern")
    private String regexPattern; // Biểu thức chính quy để kiểm tra định dạng

    @Column(name = "error_message")
    private String errorMessage; // Thông báo trả về khi vi phạm

    @Column(name = "is_read_only")
    private Boolean isReadOnly = false; // TRUE: Không cho phép sửa đổi sau khi tạo

    @Column(name = "is_required")
    private Boolean isRequired = false; // TRUE: Bắt buộc phải có giá trị

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isReadOnly == null) this.isReadOnly = false;
        if (this.isRequired == null) this.isRequired = false;
        if (this.isActive == null) this.isActive = true;
    }
}
