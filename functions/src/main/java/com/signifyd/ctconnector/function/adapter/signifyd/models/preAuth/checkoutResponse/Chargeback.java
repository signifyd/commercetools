package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Chargeback {
    @JsonProperty(value = "amount", required = true)
    public double amount;
    @JsonProperty(value = "currency", required = true)
    public String currency;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
