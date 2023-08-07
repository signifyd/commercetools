package com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.ReturnStatus;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ExecuteReturnRequestDraft implements Serializable {
  @JsonProperty("orderId")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private String orderId;
  @JsonProperty("returnId")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private String returnId;
  @JsonProperty("returnStatus")
  private ReturnStatus returnStatus;
  @JsonProperty("trackingNumbers")
  private List<String> trackingNumbers;
  @JsonProperty("refundTransactionId")
  private String refundTransactionId;
  @JsonProperty("storeCreditId")
  private String storeCreditId;
  @JsonProperty("replacementOrderId")
  private String replacementOrderId;
}