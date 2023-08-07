package com.signifyd.ctconnector.function.adapter.signifyd;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.ErrorCodes4XX;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.ErrorCodes5XX;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ErrorResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.FulfillmentRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.FulfillmentResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.CheckoutRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice.RepriceRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice.RepriceResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reroute.rerouteRequest.RerouteRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postAuth.sale.SaleRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionRequest.TransactionRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionResponse.TransactionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.recordReturn.RecordReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.recordReturn.RecordReturnResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import okhttp3.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class SignifydClient {

    private static final int TIME_OUT = 1000;

    private static final int ATTEMPT_COUNT = 2;
    private static final String API_BASE_URL = "https://api.signifyd.com/";
    private final ConfigReader configReader;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public SignifydClient(ConfigReader configReader) {
        this.configReader = configReader;
    }

    public DecisionResponse checkouts(CheckoutRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, DecisionResponse.class, SignifydApi.PRE_AUTH_CHECKOUT, "v3/orders/events/checkouts");
    }

    public TransactionResponse transaction(TransactionRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, TransactionResponse.class, SignifydApi.PRE_AUTH_TRANSACTION, "v3/orders/events/transactions");
    }

    public DecisionResponse sales(SaleRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, DecisionResponse.class, SignifydApi.POST_AUTH_SALE, "v3/orders/events/sales");
    }

    public RepriceResponse reprice(RepriceRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, RepriceResponse.class, SignifydApi.POST_SALE_REPRICE, "v3/orders/events/repricings");
    }

    public FulfillmentResponse fulfillment(FulfillmentRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, FulfillmentResponse.class, SignifydApi.POST_SALE_FULFILLMENT, "v3/orders/events/fulfillments");
    }

    public DecisionResponse reroute(RerouteRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, DecisionResponse.class, SignifydApi.POST_SALE_REROUTE, "v3/orders/events/reroutes");
    }

    public AttemptReturnResponse attemptReturn(AttemptReturnRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, AttemptReturnResponse.class, SignifydApi.ATTEMPT_RETURN, "v3/orders/events/returns/attempts");
    }

    public ExecuteReturnResponse executeReturn(ExecuteReturnRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, ExecuteReturnResponse.class, SignifydApi.EXECUTE_RETURN, "v3/orders/events/returns/executions");
    }

    public RecordReturnResponse recordReturn(RecordReturnRequestDraft draft) throws Signifyd4xxException, Signifyd5xxException {
        return this.execute(draft, RecordReturnResponse.class, SignifydApi.RECORD_RETURN, "v3/orders/events/returns/records");
    }

    private <B, T> T execute(B body, Class<T> clazz, String apiName, String path) throws Signifyd4xxException, Signifyd5xxException {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String bodyAsString = prepareRequest(body, objectMapper);
        logger.info("Signifyd Request for {} API", apiName);
        logger.info("Request Body: {}", bodyAsString);
        logger.info("Request Body: {}", bodyAsString);
        RequestBody requestBody = RequestBody.create(
                bodyAsString,
                MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_BASE_URL + path)
                .post(requestBody)
                .addHeader(SignifydApi.AUTHORIZATION_HEADER, SignifydApi.BASIC_HEADER + " " + Base64.getEncoder().encodeToString(this.configReader.getSignifydTeamAPIKey().getBytes()))
                .build();

        int signifydCheckAttempt = 0;
        boolean is5xxError;
        boolean is4xxError;
        Response response;
        String rawBody;
        int httpCode;
        try {
            do {
                response = client.newCall(request).execute();
                rawBody = response.body().string();
                httpCode = response.code();
                logger.info("Signifyd Response Http Code: {}", httpCode);
                logger.info("Signifyd Response Body: {}", rawBody);
                is5xxError = ErrorCodes5XX.is5xxError(httpCode);
                is4xxError = ErrorCodes4XX.is4xxError(httpCode);
                if (httpCode == 200 || httpCode == 201) {
                    return objectMapper.readValue(rawBody, clazz);
                } else if (is4xxError) {
                    ErrorResponse errorResponse = objectMapper.readValue(rawBody, ErrorResponse.class);
                    throw new Signifyd4xxException(errorResponse, httpCode);
                }
                signifydCheckAttempt++;
            } while (signifydCheckAttempt < ATTEMPT_COUNT && is5xxError);
            if (is5xxError) {
                ErrorResponse errorResponse = objectMapper.readValue(rawBody, ErrorResponse.class);
                throw new Signifyd5xxException(errorResponse, httpCode);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <B> String prepareRequest(B body, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
