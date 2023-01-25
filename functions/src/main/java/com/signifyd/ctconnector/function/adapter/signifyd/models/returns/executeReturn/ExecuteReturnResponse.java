package com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Coverage;
import lombok.Data;

@Data
public class ExecuteReturnResponse {
  @JsonProperty("orderId")
  private String orderId;
  @JsonProperty("returnId")
  private String returnId;
  @JsonProperty("coverage")
  private Coverage coverage;
}
