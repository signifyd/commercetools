package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Configuration {
    @JsonProperty(value = "PRE_AUTH")
    private boolean preAuth;
    @JsonProperty(value = "SCA_EVALUATION_REQUIRED")
    private boolean scaEvaluationRequired;
    @JsonProperty(value = "RECOMMENDATION_ONLY")
    private boolean recommendationOnly;
    @JsonProperty(value = "DECISION_ACTIONS",required = true)
    private DecisionActionConfigs decisionActions;
}
