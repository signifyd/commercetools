package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "streetAddress",
        "unit",
        "postalCode",
        "city",
        "provinceCode",
        "countryCode"
})
@Builder
@Data
public class Address {

    @JsonProperty("streetAddress")
    public String streetAddress;
    @JsonProperty("unit")
    public String unit;
    @JsonProperty("postalCode")
    public String postalCode;
    @JsonProperty("city")
    public String city;
    @JsonProperty("provinceCode")
    public String provinceCode;
    @JsonProperty("countryCode")
    public String countryCode;

}
