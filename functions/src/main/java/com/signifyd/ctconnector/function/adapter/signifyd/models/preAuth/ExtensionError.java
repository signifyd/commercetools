package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth;

import com.commercetools.api.models.common.LocalizedString;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtensionError {
    private String code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalizedString localizedMessage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String extensionExtraInfo;
}
