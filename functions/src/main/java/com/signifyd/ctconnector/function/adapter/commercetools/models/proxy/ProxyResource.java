package com.signifyd.ctconnector.function.adapter.commercetools.models.proxy;

import com.commercetools.api.models.order.ReturnInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ProxyResource {
    @JsonProperty("orderId")
    public String orderId;
    @JsonProperty("customerId")
    public String customerId;
    @JsonProperty("returnInfo")
    public ReturnInfo returnInfo;
}
