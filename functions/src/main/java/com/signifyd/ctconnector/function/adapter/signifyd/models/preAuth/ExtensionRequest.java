package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth;

import com.commercetools.api.models.common.Reference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExtensionRequest<T extends Reference> {
    @JsonProperty("action")
    private String action;
    @JsonProperty("resource")
    private T resource;
}