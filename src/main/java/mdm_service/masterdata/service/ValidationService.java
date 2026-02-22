package mdm_service.masterdata.service;

import jakarta.validation.ValidationException;
import mdm_service.masterdata.entity.ValidationRule;
import mdm_service.masterdata.repository.ValidationRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

@Service
public class ValidationService {

    @Autowired
    private ValidationRuleRepository ruleRepository;

    public void validate(String entityName, Object dto, Object existingEntity) {
        List<ValidationRule> rules = ruleRepository.findByEntityNameAndIsActiveTrue(entityName);

        for (ValidationRule rule : rules) {
            // Lấy giá trị từ DTO (dữ liệu mới gửi lên)
            Object newValue = getFieldValue(dto, rule.getFieldName());

            // 1. Kiểm tra Quyền Read-Only
            if (Boolean.TRUE.equals(rule.getIsReadOnly()) && existingEntity != null) {
                Object oldValue = getFieldValue(existingEntity, rule.getFieldName());

                // Nếu dữ liệu mới khác dữ liệu cũ trên một trường Read-Only -> Báo lỗi
                if (newValue != null && !newValue.equals(oldValue)) {
                    throw new ValidationException("Trường [" + rule.getFieldName() + "] là chỉ đọc (Read-only), không thể thay đổi giá trị.");
                }
            }

            // 2. Kiểm tra bắt buộc (Is Required) - Bổ sung thêm cho an toàn
            if (Boolean.TRUE.equals(rule.getIsRequired()) && (newValue == null || newValue.toString().trim().isEmpty())) {
                throw new ValidationException("Trường [" + rule.getFieldName() + "] là bắt buộc, không được để trống.");
            }

            // 3. Kiểm tra Regex
            if (StringUtils.hasText(rule.getRegexPattern()) && newValue != null) {
                if (!newValue.toString().matches(rule.getRegexPattern())) {
                    throw new ValidationException(rule.getErrorMessage() != null ?
                            rule.getErrorMessage() : "Trường " + rule.getFieldName() + " không đúng định dạng.");
                }
            }
        }
    }

    // Hàm helper để lấy giá trị field bằng Reflection an toàn
    private Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = ReflectionUtils.findField(target.getClass(), fieldName);
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                return ReflectionUtils.getField(field, target);
            }
        } catch (Exception e) {
            // Log lỗi nếu cần thiết
        }
        return null;
    }
}
