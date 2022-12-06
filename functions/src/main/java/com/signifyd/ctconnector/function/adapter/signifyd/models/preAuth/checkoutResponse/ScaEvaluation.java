package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

enum Outcome {
    REQUEST_EXEMPTION, REQUEST_EXCLUSION, DELEGATE_TO_PSP, NOT_EVALUATED
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "outcome",
        "exemptionDetails",
        "exclusionDetails"
})
@Data
public class ScaEvaluation {
    @JsonProperty(value = "outcome", required = true)
    private Outcome outcome;
    @JsonProperty("exemptionDetails")
    private ExemptionDetails exemptionDetails;
    @JsonProperty("exclusionDetails")
    private ExclusionDetails exclusionDetails;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}