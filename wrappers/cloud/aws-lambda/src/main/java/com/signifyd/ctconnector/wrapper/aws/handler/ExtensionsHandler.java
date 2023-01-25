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
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.wrapper.aws.response.ExtensionsResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import java.util.Map;

public class ExtensionsHandler implements RequestHandler<Map<String, Object>, ExtensionsResponse> {

    private static final PreAuthFunction preAuthFunction = new PreAuthFunction();
    private static final ReturnFunction returnFunction = new ReturnFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String CREATE_ACTION_TYPE = "Create";
    private static final String UPDATE_ACTION_TYPE = "Update";

    @Override
    public ExtensionsResponse handleRequest(Map<String, Object> event, Context context) {
        ExtensionsResponse response = new ExtensionsResponse();
        response.setStatusCode(HttpStatusCode.OK_200);

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode()) || event.get("body") == null) {
            return response;
        }

        try {
            String rawBody;
            var request = objectMapper.readValue(event.get("body").toString(), ExtensionRequest.class);
            switch (request.getResource().getTypeId().toString()) {
                case OrderReference.ORDER: {
                    ExtensionRequest<OrderReference> data = (ExtensionRequest<OrderReference>) request;
                    ExtensionResponse<OrderUpdateAction> result = new ExtensionResponse<OrderUpdateAction>();
                    switch (data.getAction()) {
                        case CREATE_ACTION_TYPE: {
                            Order order = ((OrderReference) data.getResource()).getObj();
                            if (!configReader.isPreAuth(order.getCountry())) {
                                response.setStatusCode(HttpStatusCode.OK_200);
                                return response;
                            }
                            result = preAuthFunction.apply(data);
                            break;
                        }
                        case UPDATE_ACTION_TYPE: {
                            result = returnFunction.apply(data);
                            break;
                        }
                        default:
                            throw new NotImplementedException(
                                    String.format("Received action type (%s) is not supported", data.getAction()));
                    }
                    if (result.isErrorResponse()) {
                        response.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
                        logger.info("Extension prevented returning with 400 code:" + result.getMessage());
                    }
                    rawBody = objectMapper.writeValueAsString(result);
                    break;
                }
                default:
                    throw new NotImplementedException(
                            String.format("Received resource type (%s) is not supported", request.getResource().getTypeId()));
            }

            response.setBody(rawBody);
            logger.debug("Sending response: {}", rawBody);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}