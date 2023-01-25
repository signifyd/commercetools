package com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Device;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Initiator;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Refund;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ReplacementItems;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ReturnedProduct;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class AttemptReturnRequestDraft implements Serializable {
  @JsonProperty("orderId")
  private String orderId;
  @JsonProperty("returnId")
  private String returnId;
  @JsonProperty("device")
  private Device device;
  @JsonProperty("returnedItems")
  private List<ReturnedProduct> returnedItems;
  @JsonProperty("replacementItems")
  private ReplacementItems replacementItems;
  @JsonProperty("refund")
  private Refund refund;
  @JsonProperty("initiator")
  private Initiator initiator;
}