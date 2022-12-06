package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DecisionActionConfigs {
    @JsonProperty("ACCEPT")
    private DecisionActionConfig accept;
    @JsonProperty("HOLD")
    private DecisionActionConfig hold;
    @JsonProperty("REJECT")
    private DecisionActionConfig reject;
}
