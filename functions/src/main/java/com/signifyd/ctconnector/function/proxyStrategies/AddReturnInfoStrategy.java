package com.signifyd.ctconnector.function.proxyStrategies;

import com.commercetools.api.client.error.BadRequestException;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.ReturnInfo;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;
import com.signifyd.ctconnector.function.utils.ProxyHelper;

import io.vrap.rmf.base.client.http.HttpStatusCode;

public class AddReturnInfoStrategy implements ProxyStrategy {
    private final CommercetoolsClient commercetoolsClient;

    public AddReturnInfoStrategy(CommercetoolsClient commercetoolsClient) {
        this.commercetoolsClient = commercetoolsClient;
    }

    @Override
    public ProxyResponse execute(ProxyRequest<ProxyResource> request) {
        try {
            ProxyHelper.validateResourceToAddReturnInfo(request.getResource());
        } catch (RuntimeException e) {
            return ProxyHelper.generateResponse(HttpStatusCode.BAD_REQUEST_400, false, e.getMessage());
        }

        String orderId = request.getResource().getOrderId();
        String customerId = request.getResource().getCustomerId();
        ReturnInfo returnInfo = request.getResource().getReturnInfo();

        Order order = null;
        try {
            order = this.commercetoolsClient.getOrderById(orderId);
        } catch (RuntimeException e) {
            return ProxyHelper.generateResponse(HttpStatusCode.BAD_REQUEST_400, false, String.format("Return info add failed: Order with %s could not find.", orderId));
        }

        if (!order.getCustomerId().equals(customerId)) {
            return ProxyHelper.generateResponse(HttpStatusCode.BAD_REQUEST_400, false, String.format("Return info add failed: Customer with %s can not acces this order.", customerId));
        }

        try {
            order = this.commercetoolsClient.addReturnInfo(order, returnInfo);
            return ProxyHelper.generateResponse(HttpStatusCode.OK_200, true, "Return info add succeed.");
        } catch (RuntimeException e) {
            BadRequestException bre = (BadRequestException) e.getCause().getCause();
            return ProxyHelper.generateResponse(HttpStatusCode.BAD_REQUEST_400, false, bre.getErrorResponse().getMessage());
        }
    }
}
