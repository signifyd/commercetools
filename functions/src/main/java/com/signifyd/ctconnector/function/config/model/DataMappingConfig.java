package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.config.model.phoneNumber.PhoneNumberField;
import lombok.Getter;

import java.util.Map;

@Getter
public class DataMappingConfig {
    @JsonProperty("PAYMENT_METHODS")
    private Map<String, String> paymentMethods;
    @JsonProperty("PHONE_NUMBER_FIELD")
    private PhoneNumberField phoneNumberField;
}
