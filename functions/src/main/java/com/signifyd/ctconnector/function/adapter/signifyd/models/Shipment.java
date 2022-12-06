package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.shipment.FulfillmentMethod;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.shipment.Origin;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "destination",
        "origin",
        "carrier",
        "minDeliveryDate",
        "maxDeliveryDate",
        "shipmentId",
})
@Builder
@Data
public class Shipment {

    @JsonProperty("destination")
    public ShipmentDestination destination;
    @JsonProperty("origin")
    private Origin origin;
    @JsonProperty("carrier")
    private String carrier;
    @JsonProperty("minDeliveryDate")
    private String minDeliveryDate;
    @JsonProperty("maxDeliveryDate")
    private String maxDeliveryDate;
    @JsonProperty("shipmentId")
    private String shipmentId;
    @JsonProperty("fulfillmentMethod")
    private FulfillmentMethod fulfillmentMethod;

}