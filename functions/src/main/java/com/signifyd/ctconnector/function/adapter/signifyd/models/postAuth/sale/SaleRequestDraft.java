package com.signifyd.ctconnector.function.adapter.signifyd.models.postAuth.sale;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.UserAccount;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Device;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Purchase;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.MerchantPlatform;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.SignifydClientInfo;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class SaleRequestDraft implements Serializable {

    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("transactions")
    private List<TransactionModel> transactions;
    @JsonProperty("purchase")
    private Purchase purchase;
    @JsonProperty("coverageRequests")
    private List<String> coverageRequests;
    @JsonProperty("decisionDelivery")
    private DecisionDelivery decisionDelivery;
    @JsonProperty("device")
    private Device device;
    @JsonProperty("userAccount")
    private UserAccount userAccount;
    @JsonProperty("merchantPlatform")
    private MerchantPlatform merchantPlatform;
    @JsonProperty("signifydClient")
    private SignifydClientInfo signifydClient;
    @JsonProperty("tags")
    private List<String> tags;
}
