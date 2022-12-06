package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Address;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Origin {
    @JsonProperty("locationId")
    private String locationId;
    @JsonProperty("address")
    private Address address;
}
