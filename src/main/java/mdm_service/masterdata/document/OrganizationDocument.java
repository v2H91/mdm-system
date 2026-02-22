package mdm_service.masterdata.document;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Document(indexName = "${elasticsearch.index.org}")
@Setting(settingPath = "elasticsearch/settings.json") // Nếu bạn có cấu hình vi_analyzer
@Getter
@Setter
public class OrganizationDocument {

    @Id
    private String id; // Lưu ID từ MySQL dưới dạng String

    @Field(type = FieldType.Keyword)
    private String taxCode;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String legalName;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String shortName;

    @Field(type = FieldType.Keyword)
    private String status;

    // Lưu danh sách địa chỉ lồng nhau (Nested) để hỗ trợ tìm kiếm mờ theo địa chỉ của tổ chức
    @Field(type = FieldType.Nested)
    private List<OrganizationAddressDocument> addresses;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String rejectedReason;

    @Getter
    @Setter
    public static class OrganizationAddressDocument {

        @Field(type = FieldType.Text, analyzer = "vi_analyzer")
        private String fullAddress;

        @Field(type = FieldType.Keyword)
        private String addressType; // HEADQUARTER, BRANCH, WAREHOUSE

        @Field(type = FieldType.Keyword)
        private String provinceCode;

        @Field(type = FieldType.Keyword)
        private String wardCode;
    }
}
