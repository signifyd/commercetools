package com.signifyd.ctconnector.function.proxyStrategies;

import com.commercetools.api.client.error.BadRequestException;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.ReturnInfo;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;

import io.vrap.rmf.base.client.http.HttpStatusCode;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;


public class AddReturnInfoStrategy implements ProxyStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public AddReturnInfoStrategy(CommercetoolsClient commercetoolsClient) {
        this.commercetoolsClient = commercetoolsClient;
    }

    @Override
    public ProxyResponse execute(ProxyRequest<ProxyResource> request) {
        String orderId = request.getResource().getOrderId();
        String customerId = request.getResource().getCustomerId();
        ReturnInfo returnInfo = request.getResource().getReturnInfo();

        if (returnInfo == null) {
            return generateResponse(HttpStatusCode.BAD_REQUEST_400, false, "Return info add failed: Return info is invalid.");
        }

        Order order = null;
        try {
            order = this.commercetoolsClient.getOrderById(orderId);
        } catch (RuntimeException e) {
            return generateResponse(HttpStatusCode.BAD_REQUEST_400, false, String.format("Return info add failed: Order with %s could not find.", orderId));
        }

        if (!order.getCustomerId().equals(customerId)) {
            return generateResponse(HttpStatusCode.BAD_REQUEST_400, false, String.format("Return info add failed: Customer with %s can not acces this order.", customerId));
        }

        try {
            order = this.commercetoolsClient.addReturnInfo(order, returnInfo);
            return generateResponse(HttpStatusCode.OK_200, true, "Return info add succeed.");
        } catch (RuntimeException e) {
            BadRequestException bre = (BadRequestException) e.getCause().getCause();
            return generateResponse(HttpStatusCode.BAD_REQUEST_400, false, bre.getErrorResponse().getMessage());
        }
    }

    private ProxyResponse generateResponse(int statusCode, boolean succeed, String message) {
        if (succeed) logger.info(message);
        else logger.error(message);
        return ProxyResponse.builder()
                .statusCode(statusCode)
                .succeed(succeed)
                .message(message)
                .build();
    }
}
