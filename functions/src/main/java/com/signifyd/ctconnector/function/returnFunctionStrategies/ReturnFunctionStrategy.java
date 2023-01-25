package com.signifyd.ctconnector.function.returnFunctionStrategies;

import java.io.IOException;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.commercetools.api.models.order.ReturnInfo;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;

public interface ReturnFunctionStrategy {
    public ExtensionResponse<OrderUpdateAction> execute(Order order, ReturnInfo returnInfo) throws Signifyd4xxException, Signifyd5xxException, IOException;
}
