package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"checkoutId", "orderId", "transactions"})
@Builder
@Data
public class TransactionRequestDraft implements Serializable {

    @JsonProperty("checkoutId")
    private String checkoutId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("transactions")
    private List<TransactionModel> transactions;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
