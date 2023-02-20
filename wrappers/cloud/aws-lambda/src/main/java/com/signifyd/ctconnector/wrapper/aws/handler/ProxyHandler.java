package com.signifyd.ctconnector.wrapper.aws.handler;

import io.vrap.rmf.base.client.http.HttpStatusCode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.ProxyFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.wrapper.aws.response.ExtensionsResponse;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import java.util.Map;

public class ProxyHandler implements RequestHandler<Map<String, Object>, ExtensionsResponse> {
    private static final ProxyFunction function = new ProxyFunction();
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
            ProxyRequest<ProxyResource> request = objectMapper.readValue(event.get("body").toString(), new TypeReference<ProxyRequest<ProxyResource>>() {});
            ProxyResponse result = function.apply(request);
            if (!result.isSucceed()) {
                response.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
                logger.info("Proxy prevented returning with 400 code:" + result.getMessage());
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