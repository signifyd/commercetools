package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.FulfillmentStatus;

import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"orderId",
"fullfilmentStatus"
})

@Data
@Builder
public class FulfillmentRequestDraft {

    @JsonProperty("orderId")
    public String orderId;
    @JsonProperty("fulfillmentStatus")
    public FulfillmentStatus fulfillmentStatus;
    @JsonProperty("fulfillments")
    public List<Fulfillment> fulfillments;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

}
