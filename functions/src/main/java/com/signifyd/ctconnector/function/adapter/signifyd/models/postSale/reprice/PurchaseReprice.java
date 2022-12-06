package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Product;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"createdAt", "orderChannel", "totalPrice", "currency", "products", "confirmationPhone"})
@Builder
@Data
public class PurchaseReprice implements Serializable {
    @JsonProperty("totalPrice")
    private Double totalPrice;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("products")
    private List<Product> products = null;
    @JsonProperty("totalShippingCost")
    private Double totalShippingCost;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}