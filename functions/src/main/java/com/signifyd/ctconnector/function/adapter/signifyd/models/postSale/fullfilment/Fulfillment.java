package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.ShipmentStatus;

import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "shipmentId",
    "shippedAt",
    "shipmentStatus",
    "trackingUrls",
    "trackingNumbers",
    "carrier"
})

@Data
@Builder
public class Fulfillment {

    @JsonProperty("shipmentId")
    public String shipmentId;
    @JsonProperty("shippedAt")
    public String shippedAt;
    @JsonProperty("shipmentStatus")
    public ShipmentStatus shipmentStatus;
    @JsonProperty("products")
    public List<FulfillmentProduct> products;
    @JsonProperty("trackingUrls")
    public List<String> trackingUrls;
    @JsonProperty("trackingNumbers")
    public List<String> trackingNumbers;
    @JsonProperty("destination")
    public FulfillmentDestination destination;
    @JsonProperty("carrier")
    public String carrier;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

}
