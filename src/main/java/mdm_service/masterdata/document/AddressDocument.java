package mdm_service.masterdata.document;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "mdm-addresses")
@Setting(settingPath = "elasticsearch/settings.json")
@Getter
@Setter
public class AddressDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String houseNumber;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String street;

    @Field(type = FieldType.Keyword)
    private String wardCode;

    @Field(type = FieldType.Keyword)
    private String provinceCode;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
    private String fullAddress; // Trường quan trọng nhất để search mờ
}
