package mdm_service.masterdata.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "mdm-organization-histories")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrgHistoryDocument {
    @Id
    private String id; // ID của bản ghi lịch sử

    @Field(type = FieldType.Keyword)
    private String orgId; // ID của tổ chức để filter theo Timeline

    @Field(type = FieldType.Keyword)
    private String actionType; // CREATE, UPDATE, APPROVE, REJECT

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String changes; // Lưu chuỗi JSON thay đổi để hiển thị nhanh

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String reason; // Lý do Reject để thống kê

    @Field(type = FieldType.Keyword)
    private String performedBy; // Người thực hiện

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime performedAt; // Thời điểm thực hiện
}
