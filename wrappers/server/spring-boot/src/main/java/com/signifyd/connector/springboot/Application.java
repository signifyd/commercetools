package com.signifyd.connector.springboot;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.connector.springboot.models.SnsNotificationMessage;
import com.signifyd.connector.springboot.models.SnsSubscriptionConfirmationMessage;
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.ProxyFunction;
import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.WebhookFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import com.signifyd.ctconnector.function.utils.SignifydWebhookValidator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootApplication
@RestController
public class Application {
    private final ConfigReader configReader;
    private final PreAuthFunction preAuthFunction;
    private final ProxyFunction proxyFunction;
    private final ReturnFunction returnFunction;
    private final SubscriptionFunction subscriptionFunction;
    private final WebhookFunction webhookFunction;
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public Application() {
        this.configReader = new ConfigReader();
        CommercetoolsClient commercetoolsClient = new CommercetoolsClient(configReader);
        SignifydClient signifydClient = new SignifydClient(configReader);
        PropertyReader propertyReader = new PropertyReader();
        SignifydMapper signifydMapper = new SignifydMapper();

        this.preAuthFunction = new PreAuthFunction(configReader, commercetoolsClient, signifydClient, propertyReader, signifydMapper);
        this.proxyFunction = new ProxyFunction(commercetoolsClient);
        this.returnFunction = new ReturnFunction(signifydClient, signifydMapper);
        this.subscriptionFunction = new SubscriptionFunction(configReader, commercetoolsClient, signifydClient);
        this.webhookFunction = new WebhookFunction(configReader, commercetoolsClient);
    }

    @PostMapping("/preauth")
    public ResponseEntity<ExtensionResponse<OrderUpdateAction>> preAuth(@RequestBody ExtensionRequest<OrderReference> request) {
        var result = this.preAuthFunction.apply(request);
        if (result.isErrorResponse()) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/return/attempt")
    public ResponseEntity<ExtensionResponse<OrderUpdateAction>> returnAttempt(@RequestBody ExtensionRequest<OrderReference> request) {
        ExtensionResponse<OrderUpdateAction> response = this.returnFunction.apply(request);
        if (response.isErrorResponse()) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/proxy")
    public ResponseEntity<ProxyResponse> proxy(@RequestBody ProxyRequest<ProxyResource> request) {
        ProxyResponse response = this.proxyFunction.apply(request);
        HttpStatus httpStatus = response.isSucceed() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping(value = "/subscriptions/pubsub",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> pubsubSubscriptions(
            HttpServletRequest req
    ) throws Exception {
        JsonNode bodyNode = objectMapper.readTree(req.getReader());
        String base64Data = bodyNode.get("message")
                .get("data").asText();
        byte[] data = Base64.getDecoder().decode(base64Data);
        Message message = objectMapper.readValue(
                data, Message.class);
        this.subscriptionFunction.apply(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/subscriptions/eventgrid", headers = {"aeg-event-type=Notification"})
    public ResponseEntity<?> eventGridSubscriptions(
            HttpServletRequest req
    ) throws Exception {
        JsonNode bodyNode = objectMapper.readTree(req.getReader());
        String data = bodyNode.get("data").toString();
        Message message = objectMapper.readValue(
                data, Message.class);
        this.subscriptionFunction.apply(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.OPTIONS, value = "/subscriptions/eventgrid")
    public ResponseEntity<String> eventGridSubscriptionValidation()  {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("WebHook-Allowed-Origin", "*");
        responseHeaders.set("WebHook-Allowed-Rate", "200");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body("Response with header using ResponseEntity");
    }


    @PostMapping(value = "/subscription/sns")
    public void snsConfirmation(HttpServletRequest request) throws IOException {
        SnsSubscriptionConfirmationMessage message = objectMapper.readValue(request.getInputStream(), SnsSubscriptionConfirmationMessage.class);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request confirmationRequest = new Request.Builder()
                .url(message.getSubscribeURL())
                .get()
                .build();
        Response response = client.newCall(confirmationRequest).execute();
        if (response.isSuccessful()) {
            logger.info("Subscribed to {} topic successfully", message.getTopicArn());
        } else {
            logger.error("Failed on Subscribing to {} topic", message.getTopicArn());
        }

    }

    @PostMapping(value = "/subscription/sns", headers = {"x-amz-sns-message-type=Notification"})
    public void snsNotification(HttpServletRequest request) throws IOException {
        SnsNotificationMessage notificationMessage = objectMapper.readValue(request.getInputStream(), SnsNotificationMessage.class);
        Message subscriptionMessage = objectMapper.readValue(notificationMessage.getSnsMessage(), Message.class);
        subscriptionFunction.apply(subscriptionMessage);
    }


    @PostMapping(path = "/webhooks", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public String webhooks(@RequestHeader(value = SignifydApi.SHA_256_HEADER, required = false) String signifydSecHmacSha256,
                           @RequestHeader(value = SignifydApi.CHECK_POINT_HEADER, required = false) String signifydCheckpoint,
                           @RequestBody DecisionResponse response,
                           HttpEntity<String> httpEntity) {
        String rawBody = httpEntity.getBody();
        if (!SignifydWebhookValidator.validateWebhook(signifydSecHmacSha256, rawBody, configReader.getSignifydTeamAPIKey())) {
            throw new IllegalArgumentException("Webhook is not valid.");
        }
        WebhookRequest webhookRequest =
                WebhookRequest.builder()
                        .decisionResponse(response)
                        .signifydCheckpoint(signifydCheckpoint)
                        .build();
        return this.webhookFunction.apply(webhookRequest);
    }


}
