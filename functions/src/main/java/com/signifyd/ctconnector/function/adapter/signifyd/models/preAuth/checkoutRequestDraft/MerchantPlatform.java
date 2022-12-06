package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "version"
})
@Data
@Builder
public class MerchantPlatform {
    @JsonProperty("name")
    public String name;
    @JsonProperty("version")
    private String version;
}