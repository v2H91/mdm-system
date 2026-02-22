package mdm_service.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationDTO {
    private String wardCode;
    private String wardName;
    private String wardType;
    private String provinceCode;
    private String provinceName;
}
