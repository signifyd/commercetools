package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"orderId", "purchase"})
@Builder
@Data
public class RepriceRequestDraft implements Serializable {
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("purchase")
    private PurchaseReprice purchase;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
