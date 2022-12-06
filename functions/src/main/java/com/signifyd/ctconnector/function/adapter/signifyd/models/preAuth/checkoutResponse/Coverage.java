package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "fraudChargebacks",
        "inrChargebacks",
        "allChargebacks"
})
@Data
public class Coverage {
    @JsonProperty("fraudChargebacks")
    public Chargeback fraudChargebacks;
    @JsonProperty("inrChargebacks")
    public Chargeback inrChargebacks;
    @JsonProperty("allChargebacks")
    public Chargeback allChargebacks;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}