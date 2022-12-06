package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

enum Exemption {
    TRA, LOW_VALUE
}

enum Placement {
    AUTHENTICATION, AUTHORIZATION
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "exemption",
        "placement"
})
@Data
public class ExemptionDetails {
    @JsonProperty(value = "exemption", required = true)
    private Exemption exemption;
    @JsonProperty(value = "placement", required = true)
    private Placement placement;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}