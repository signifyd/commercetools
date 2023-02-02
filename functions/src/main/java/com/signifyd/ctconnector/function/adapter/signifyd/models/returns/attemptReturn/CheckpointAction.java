package com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;

import lombok.Builder;

@Builder
public class CheckpointAction {
  @JsonProperty(value = "returnId", required = true)
  private String returnId;
  @JsonProperty(value = "checkpointAction", required = true)
  public Action checkpointAction;
}
