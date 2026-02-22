package mdm_service.masterdata.constant;

import lombok.Getter;

@Getter
public enum LocationType {
    PROVINCE(0),
    WARD(1);

    private final int value;

    LocationType(int value) {
        this.value = value;
    }

    public static LocationType fromValue(int value) {
        for (LocationType type : LocationType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown LocationType value: " + value);
    }
}