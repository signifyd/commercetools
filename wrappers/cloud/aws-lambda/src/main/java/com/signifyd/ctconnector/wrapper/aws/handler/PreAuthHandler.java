package com.signifyd.ctconnector.wrapper.aws.handler;

import io.vrap.rmf.base.client.http.HttpStatusCode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.wrapper.aws.response.ExtensionsResponse;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import java.util.Map;

public class PreAuthHandler implements RequestHandler<Map<String, Object>, ExtensionsResponse> {

    private static final PreAuthFunction function = new PreAuthFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ExtensionsResponse handleRequest(Map<String, Object> event, Context context) {
        ExtensionsResponse response = new ExtensionsResponse();
        response.setStatusCode(HttpStatusCode.OK_200);

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode()) || event.get("body") == null) {
            return response;
        }

        try {
            ExtensionRequest<OrderReference> request = objectMapper.readValue(event.get("body").toString(), ExtensionRequest.class);
            Order order = ((OrderReference) request.getResource()).getObj();
            if (!configReader.isPreAuth(order.getCountry())) {
                response.setStatusCode(HttpStatusCode.OK_200);
                return response;
            }
            ExtensionResponse<OrderUpdateAction> result = function.apply(request);
            if (result.isErrorResponse()) {
                result.setResponseType(result.FAILED_VALIDATION);
                response.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
                logger.info("PreAuth prevented returning with 400 code:" + objectMapper.writeValueAsString(result.getErrors()));
            }
            String rawBody = objectMapper.writeValueAsString(result);
            response.setBody(rawBody);
            logger.debug("Sending response: {}", rawBody);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}