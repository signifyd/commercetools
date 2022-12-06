package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reroute.rerouteRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Device;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Shipment;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"orderId", "device", "shipments"})
@Builder
@Data
public class RerouteRequestDraft implements Serializable {
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("device")
    private Device device;
    @JsonProperty("shipments")
    private List<Shipment> shipments;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}