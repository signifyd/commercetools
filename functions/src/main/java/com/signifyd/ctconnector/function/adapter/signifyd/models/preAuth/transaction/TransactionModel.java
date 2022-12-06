package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.GatewayStatusCode;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "transactionId",
    "gatewayStatusCode",
    "paymentMethod",
    "amount",
    "currency"
})
@Builder
@Data
public class TransactionModel implements Serializable {

    @JsonProperty("transactionId")
    private String transactionId;
    @JsonProperty("gatewayStatusCode")
    private GatewayStatusCode gatewayStatusCode;
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    @JsonProperty("checkoutPaymentDetails")
    private CheckoutPaymentDetails checkoutPaymentDetails;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("gateway")
    private String gateway;
    @JsonProperty("gatewayStatusMessage")
    private String gatewayStatusMessage;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}