package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "default",
        "overriding"
})
@Data
public class Policies {
    @JsonProperty(value = "default", required = true)
    public Policy defaultPolicy;
    @JsonProperty(value = "overriding", required = true)
    public List<Policy> overridingPolicyContainer = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}