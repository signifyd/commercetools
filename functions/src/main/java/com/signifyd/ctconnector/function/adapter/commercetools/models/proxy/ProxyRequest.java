package com.signifyd.ctconnector.function.adapter.commercetools.models.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProxyRequest<T> {
    @JsonProperty("action")
    private String action;
    @JsonProperty("resource")
    private T resource;
}