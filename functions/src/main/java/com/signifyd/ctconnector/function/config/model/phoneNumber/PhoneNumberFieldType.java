package com.signifyd.ctconnector.function.config.model.phoneNumber;

public enum PhoneNumberFieldType {
    BILLING,
    SHIPPING,
    CUSTOM;

    public static PhoneNumberFieldType getFieldTypeEnum(String fieldTypeString) {
        for (PhoneNumberFieldType fieldTypeEnum : PhoneNumberFieldType.values()) {
            if (fieldTypeString.equals(fieldTypeEnum.name())) {
                return fieldTypeEnum;
            }
        }
        return CUSTOM;
    }
}
