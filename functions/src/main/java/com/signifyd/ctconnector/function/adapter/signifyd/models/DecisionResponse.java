package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Coverage;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.ScaEvaluation;
import lombok.Data;

@Data
public class DecisionResponse {
    @JsonProperty("signifydId")
    private String signifydId;
    @JsonProperty("checkoutId")
    private String checkoutId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("decision")
    private Decision decision;
    @JsonProperty("coverage")
    private Coverage coverage;
    @JsonProperty("scaEvaluation")
    private ScaEvaluation scaEvaluation;
}
