package com.signifyd.ctconnector.function.utils;

import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;

import ch.qos.logback.classic.Logger;

import java.lang.invoke.MethodHandles;

import org.slf4j.LoggerFactory;

public class ProxyHelper {
    static Logger logger = (Logger) LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void validateRequest(ProxyRequest<ProxyResource> request) {
        if (request.getAction() == null || request.getResource() == null)
            throw new RuntimeException("Resource is not valid to add return info.");
    }

    public static void validateResourceToAddReturnInfo(ProxyResource resource) {
        if (resource.getCustomerId() == null)
            throw new RuntimeException("Add return info failed: customerId is mandatory.");
        if (resource.getOrderId() == null)
            throw new RuntimeException("Add return info failed: orderId is mandatory.");
        if (resource.getReturnInfo() == null)
            throw new RuntimeException("Add return info failed: returnInfo is mandatory.");
    }

    public static ProxyResponse generateResponse(int statusCode, boolean succeed, String message) {
        if (succeed)
            logger.info(message);
        else
            logger.error(message);
        return ProxyResponse.builder()
                .statusCode(statusCode)
                .succeed(succeed)
                .message(message)
                .build();
    }
}
