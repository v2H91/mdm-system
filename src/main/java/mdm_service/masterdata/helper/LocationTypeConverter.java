package mdm_service.masterdata.helper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import mdm_service.masterdata.constant.LocationType;

@Converter(autoApply = true) // Tự động áp dụng cho tất cả các field kiểu LocationType
public class LocationTypeConverter implements AttributeConverter<LocationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LocationType attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public LocationType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return LocationType.fromValue(dbData);
    }
}