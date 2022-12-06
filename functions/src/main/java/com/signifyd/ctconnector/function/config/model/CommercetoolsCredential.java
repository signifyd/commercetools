package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommercetoolsCredential {
    @JsonProperty(value = "CLIENT_ID",required = true)
    private String clientId;
    @JsonProperty(value = "CLIENT_SECRET",required = true)
    private String clientSecret;
    @JsonProperty(value = "PROJECT_KEY",required = true)
    private String projectKey;
    @JsonProperty(value = "REGION",required = true)
    private String region;
}
