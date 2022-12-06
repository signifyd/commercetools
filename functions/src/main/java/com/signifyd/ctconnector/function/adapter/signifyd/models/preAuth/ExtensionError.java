package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtensionError {
    private String code;
    private String message;
}
