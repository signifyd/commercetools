package com.signifyd.ctconnector.function.adapter.signifyd.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class Purchase implements Serializable {

    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("orderChannel")
    private String orderChannel;
    @JsonProperty("totalPrice")
    private Double totalPrice;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("products")
    private List<Product> products = null;
    @JsonProperty("confirmationPhone")
    private String confirmationPhone;
    @JsonProperty("confirmationEmail")
    private String confirmationEmail;
    @JsonProperty("totalShippingCost")
    private Double totalShippingCost;
    @JsonProperty("shipments")
    private List<Shipment> shipments;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}