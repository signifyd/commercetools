package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BillingAddress {
    @JsonProperty("streetAddress")
    private String streetAddress;
    @JsonProperty("unit")
    private String unit;
    @JsonProperty("postalCode")
    private String postalCode;
    @JsonProperty("city")
    private String city;
    @JsonProperty("provinceCode")
    private String provinceCode;
    @JsonProperty("countryCode")
    private String countryCode;
}
