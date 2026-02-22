package mdm_service.masterdata.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mdm_service.masterdata.constant.ActionType;
import mdm_service.masterdata.document.OrgHistoryDocument;
import mdm_service.masterdata.dto.request.OrganizationRequest;
import mdm_service.masterdata.entity.Organization;
import mdm_service.masterdata.entity.OrganizationHistory;
import mdm_service.masterdata.repository.OrganizationHistoryRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationHistoryService {

    private final OrganizationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public void recordUpdateHistory(Organization oldOrg, OrganizationRequest newDto, String userEmail) {
        Map<String, Map<String, Object>> changeMap = new HashMap<>();

        // So sánh từng trường thông tin chính
        compareAndLog(changeMap, "legalName", oldOrg.getLegalName(), newDto.legalName());
        compareAndLog(changeMap, "shortName", oldOrg.getShortName(), newDto.shortName());
        compareAndLog(changeMap, "taxCode", oldOrg.getTaxCode(), newDto.taxCode());

        // Nếu có sự thay đổi mới tiến hành lưu vào DB
        if (!changeMap.isEmpty()) {
            try {
                OrganizationHistory history = OrganizationHistory.builder()
                        .organization(oldOrg)
                        .actionType(ActionType.UPDATE)
                        .changes(objectMapper.writeValueAsString(changeMap))
                        .performedBy(userEmail)
                        .performedAt(LocalDateTime.now())
                        .build();

                this.saveHistoryAndIndex(history);

            } catch (JsonProcessingException e) {
                log.error("Lỗi khi chuyển đổi JSON lịch sử: {}", e.getMessage());
            }
        }
    }

    private void compareAndLog(Map<String, Map<String, Object>> map, String field, Object oldVal, Object newVal) {
        // Xử lý null để tránh NullPointerException và so sánh chính xác
        String oldStr = (oldVal == null) ? "" : oldVal.toString().trim();
        String newStr = (newVal == null) ? "" : newVal.toString().trim();

        if (!oldStr.equals(newStr)) {
            Map<String, Object> values = new HashMap<>();
            values.put("old", oldVal);
            values.put("new", newVal);
            map.put(field, values);
        }
    }

    public void save(OrganizationHistory history) {
        this.saveHistoryAndIndex(history);
    }

    @Transactional
    public void saveHistoryAndIndex(OrganizationHistory history) {
        // 1. Lưu vào MySQL
        historyRepository.save(history);

        // 2. Chuyển đổi và lưu lên Elasticsearch
        OrgHistoryDocument doc = OrgHistoryDocument.builder()
                .id(history.getId().toString())
                .orgId(history.getOrganization().getId().toString())
                .actionType(history.getActionType().name())
                .changes(history.getChanges())
                .reason(history.getReason())
                .performedBy(history.getPerformedBy())
                .performedAt(history.getPerformedAt())
                .build();

        elasticsearchOperations.save(doc);

        log.info("Trace - History indexed to ES for Org ID: {}", history.getOrganization().getId());
    }

    public List<OrgHistoryDocument> getTimeline(Long orgId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("orgId").value(orgId)))
                .withSort(s -> s.field(f -> f.field("performedAt").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(query, OrgHistoryDocument.class)
                .stream().map(SearchHit::getContent).toList();
    }
}
