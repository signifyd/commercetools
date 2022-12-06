package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DecisionActionConfig {
    @JsonProperty(value = "ACTION_TYPE", required = true)
    private ActionType actionType;
    @JsonProperty("DEFAULT_STATE")
    private String defaultState;
    @JsonProperty("CUSTOM_STATE_KEY")
    private String customStateKey;
    @JsonProperty("FORCE_TRANSITION")
    private boolean forceTransition;
}