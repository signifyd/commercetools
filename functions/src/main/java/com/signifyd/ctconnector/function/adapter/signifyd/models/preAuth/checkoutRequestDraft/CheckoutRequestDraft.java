package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.models.UserAccount;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Device;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Purchase;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"checkoutId", "orderId", "transactions", "purchase", "coverageRequests", "additionalEvalRequests", "merchantPlatform",
        "signifydClient", "tags"})
@Builder
@Data
public class CheckoutRequestDraft implements Serializable {

    @JsonProperty("checkoutId")
    private String checkoutId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("transactions")
    private List<TransactionModel> transactions;
    @JsonProperty("purchase")
    private Purchase purchase;
    @JsonProperty("coverageRequests")
    private List<String> coverageRequests;
    @JsonProperty("device")
    private Device device;
    @JsonProperty("userAccount")
    private UserAccount userAccount;
    @JsonProperty("additionalEvalRequests")
    private List<EAdditionalEvalRequests> additionalEvalRequests;
    @JsonProperty("merchantPlatform")
    private MerchantPlatform merchantPlatform;
    @JsonProperty("signifydClient")
    private SignifydClientInfo signifydClient;
    @JsonProperty("tags")
    private List<String> tags;
}
