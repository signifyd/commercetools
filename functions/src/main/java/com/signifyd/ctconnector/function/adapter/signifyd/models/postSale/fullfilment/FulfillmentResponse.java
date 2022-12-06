package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "shipmentIds"
})

public class FulfillmentResponse {

    @JsonProperty("orderId")
    public String orderId;
    @JsonProperty("shipmentIds")
    public List<String> shipmentIds;

}
