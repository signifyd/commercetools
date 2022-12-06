package com.signifyd.ctconnector.function.config.model.phoneNumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PhoneNumberField {
    @JsonProperty(value = "customerPhoneNumberField")
    private String customerPhoneNumberField;
}
