package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "provider",
        "payload",
        "payloadEncoding",
        "payloadVersion"
})
@Data
@Builder
public class Fingerprint {
    @JsonProperty("provider")
    private String provider;
    @JsonProperty("payload")
    private String payload;
    @JsonProperty("payloadEncoding")
    private String payloadEncoding;
    @JsonProperty("payloadVersion")
    private String payloadVersion;
}