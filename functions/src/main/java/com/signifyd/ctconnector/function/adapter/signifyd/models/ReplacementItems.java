package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ReplacementItems implements Serializable {
    @JsonProperty("products")
    private List<Product> products;
    @JsonProperty("shipments")
    private List<Shipment> shipments;
}