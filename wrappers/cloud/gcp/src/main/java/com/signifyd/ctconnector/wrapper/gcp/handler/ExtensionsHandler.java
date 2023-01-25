package com.signifyd.ctconnector.wrapper.gcp.handler;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

import io.vrap.rmf.base.client.http.HttpStatusCode;

import java.io.PrintWriter;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionsHandler implements HttpFunction {
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final PreAuthFunction preAuthFunction = new PreAuthFunction();
    private static final ReturnFunction returnFunction = new ReturnFunction();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConfigReader configReader = new ConfigReader();

    private static final String CREATE_ACTION_TYPE = "Create";
    private static final String UPDATE_ACTION_TYPE = "Update";

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        String requestBody = httpRequest.getReader().lines().collect(Collectors.joining());
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode()) || requestBody == null) {
            httpResponse.setStatusCode(HttpStatusCode.OK_200);
            return;
        }

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String rawBody;
            var request = objectMapper.readValue(requestBody, ExtensionRequest.class);
            switch (request.getResource().getTypeId().toString()) {
                case OrderReference.ORDER: {
                    ExtensionRequest<OrderReference> data = (ExtensionRequest<OrderReference>) request;
                    ExtensionResponse<OrderUpdateAction> result = new ExtensionResponse<OrderUpdateAction>();
                    switch (data.getAction()) {
                        case CREATE_ACTION_TYPE: {
                            Order order = ((OrderReference) data.getResource()).getObj();
                            if (!configReader.isPreAuth(order.getCountry())) {
                                httpResponse.setStatusCode(HttpStatusCode.OK_200);
                                return;
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
                        httpResponse.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
                        logger.info("Extension prevented returning with 400 code:" + result.getMessage());
                    }
                    rawBody = objectMapper.writeValueAsString(result);
                    break;
                }
                default:
                    throw new NotImplementedException(
                            String.format("Received resource type (%s) is not supported", request.getResource().getTypeId()));
            }

            PrintWriter writer = new PrintWriter(httpResponse.getWriter());
            httpResponse.setContentType("application/json");
            writer.printf(rawBody);
            logger.debug("Sending response: {}", rawBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
