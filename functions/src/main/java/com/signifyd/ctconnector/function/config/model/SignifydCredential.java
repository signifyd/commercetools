package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SignifydCredential {
    @JsonProperty(value = "TEAM_API_KEY",required = true)
    private String teamAPIKey;
}
