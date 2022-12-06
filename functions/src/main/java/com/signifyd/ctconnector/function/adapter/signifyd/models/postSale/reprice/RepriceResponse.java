package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Coverage;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import lombok.Data;

@Data
public class RepriceResponse {
    @JsonProperty("signifydId")
    private String signifydId;
    @JsonProperty("decision")
    private Decision decision;
    @JsonProperty("coverage")
    private Coverage coverage;
}
