package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "itemName",
    "itemQuantity",
    "itemId",
    "passengerId",
    "airlineTicketFulfillmentStatus"
})

@Data
@Builder
public class FulfillmentProduct {

    @JsonProperty("itemName")
    public String itemName;
    @JsonProperty("itemQuantity")
    public Long itemQuantity;
    @JsonProperty("itemId")
    public String itemId;
    @JsonProperty("passengerId")
    public String passengerId;
    @JsonProperty("airlineTicketFulfillmentStatus")
    public String airlineTicketFulfillmentStatus;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

}
