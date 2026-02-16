package mdm_service.masterdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationService {
    //private final ValidationRuleRepository ruleRepository;

    public void validate(String entity, String field, String value) {
        // TODO: Nên dùng Redis để cache list rules này, tránh query DB liên tục
//        ruleRepository.findByEntityNameAndFieldName(entity, field)
//                .ifPresent(rule -> {
//                    if (!value.matches(rule.getRegexPattern())) {
//                        throw new BusinessException(rule.getErrorMessage());
//                    }
//                });
    }
}
