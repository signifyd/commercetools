package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

enum Exclusion {
    ONE_LEG_OUT, MIT, MOTO, ANONYMOUS_PREPAID
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "exclusion"
})
@Data
public class ExclusionDetails {
    @JsonProperty(value = "exclusion", required = true)
    private Exclusion exclusion;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}