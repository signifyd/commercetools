package com.signifyd.ctconnector.function.adapter.commercetools.models.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ProxyResponse {
    @JsonProperty("statusCode")
    private int statusCode;
    @JsonProperty("message")
    private String message;
    @JsonProperty("succeed")
    private boolean succeed;
}

