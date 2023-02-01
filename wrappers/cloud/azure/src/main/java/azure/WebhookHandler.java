package azure;

import java.util.*;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.signifyd.ctconnector.function.WebhookFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import org.slf4j.LoggerFactory;

/**
 * Azure Functions with HTTP Trigger.
 */
public class WebhookHandler {
    private static final WebhookFunction function = new WebhookFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FunctionName("webhook")
    public HttpResponseMessage run(
            @HttpTrigger(name = "webhook",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())
                || request.getBody().isEmpty()
                || !request.getHeaders().containsKey(SignifydApi.SHA_256_HEADER)
                || !request.getHeaders().containsKey(SignifydApi.CHECK_POINT_HEADER)) {
            return request.createResponseBuilder(HttpStatus.OK).build();
        }
        String rawBody = request.getBody().orElseThrow();
        try {
            var response = objectMapper.readValue(rawBody, DecisionResponse.class);
            String signifydCheckpoint = request.getHeaders().get(SignifydApi.CHECK_POINT_HEADER);
            WebhookRequest webhookRequest =
                    WebhookRequest.builder()
                            .decisionResponse(response)
                            .signifydCheckpoint(signifydCheckpoint)
                            .build();
            logger.info("Signifyd Webhook payload: {}", objectMapper.writeValueAsString(response));
            function.apply(webhookRequest);
            return request.createResponseBuilder(HttpStatus.OK).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
