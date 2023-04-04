package com.signifyd.ctconnector.function;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.common.Money;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.error.InvalidInputError;
import com.commercetools.api.models.order.*;
import com.commercetools.api.models.payment.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.CoverageRequests;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperFactory;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperType;
import com.signifyd.ctconnector.function.adapter.signifyd.models.*;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionError;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.EAdditionalEvalRequests;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.MerchantPlatform;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.SignifydClientInfo;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.CheckoutRequestDraft;
import com.signifyd.ctconnector.function.decisionCommands.AcceptCommand;
import com.signifyd.ctconnector.function.decisionCommands.DecisionCommand;
import com.signifyd.ctconnector.function.decisionCommands.HoldCommand;
import com.signifyd.ctconnector.function.decisionCommands.RejectCommand;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.config.model.ActionType;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

public class PreAuthFunction
        implements Function<ExtensionRequest<OrderReference>, ExtensionResponse<OrderUpdateAction>> {
    private static final String ACTION_TYPE = "Create";

    private final ConfigReader configReader;
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final PropertyReader propertyReader;
    private final SignifydMapper signifydMapper;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public PreAuthFunction() {
        this.configReader = new ConfigReader();
        this.commercetoolsClient = new CommercetoolsClient(configReader);
        this.signifydClient = new SignifydClient(configReader);
        this.propertyReader = new PropertyReader();
        this.signifydMapper = new SignifydMapper();
    }

    public PreAuthFunction(ConfigReader configReader,
            CommercetoolsClient commercetoolsClient,
            SignifydClient signifydClient,
            PropertyReader propertyReader,
            SignifydMapper signifydMapper) {
        this.configReader = configReader;
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.propertyReader = propertyReader;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> apply(ExtensionRequest<OrderReference> request) {
        validateExtensionRequest(request);
        Order order = request.getResource().getObj();
        Payment payment = this.commercetoolsClient.getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(order));

        if (!isOrderReadyForCheckoutApiCall(order)) {
            return new ExtensionResponse<OrderUpdateAction>(new ArrayList<>());
        }

        if (payment != null && !isOrderEligibleToProcess(payment)) {
            ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<>();
            response.addAction(OrderSetCustomFieldActionBuilder.of()
                    .name(CustomFields.IS_SENT_TO_SIGNIFYD)
                    .value(false)
                    .build());
            return response;
        }

        order.getCustom().getFields().values().put(CustomFields.CHECKOUT_ID, UUID.randomUUID().toString());
        Customer customer = order.getCustomerId() != null
                ? this.commercetoolsClient.getCustomerById(order.getCustomerId())
                : null;

        return sendRequest(order, payment, customer);
    }

    private CheckoutRequestDraft generateRequest(Order order, Payment payment, Customer customer) {
        TransactionMapperFactory transactionMapperFactory = new TransactionMapperFactory(order, payment, configReader);
        var draftBuilder = CheckoutRequestDraft
                .builder()
                .checkoutId(order.getCustom().getFields().values().get(CustomFields.CHECKOUT_ID).toString())
                .orderId(order.getId())
                .transactions(transactionMapperFactory.generateTransactionMapper(TransactionMapperType.PRE_AUTH)
                        .generateTransactions())
                .purchase(this.signifydMapper.mapPurchaseFromCommercetools(customer, order,
                        configReader.getPhoneNumberFieldMapping()))
                .device(this.signifydMapper.mapDeviceFromCommercetools(order))
                .userAccount(this.signifydMapper.mapUserAccountFromCommercetools(customer,
                        configReader.getPhoneNumberFieldMapping()))
                .merchantPlatform(MerchantPlatform
                        .builder()
                        .name(SignifydApi.MERCHANT_PLATFORM)
                        .version(this.propertyReader.getCommercetoolsSDKVersion())
                        .build())
                .signifydClient(SignifydClientInfo
                        .builder()
                        .application(SignifydApi.SIGNIFYD_CLIENT_INFO)
                        .version(this.propertyReader.getSignifydClientVersion())
                        .build())
                .coverageRequests(Collections.singletonList(CoverageRequests.FRAUD.name()));

        if (configReader.isScaEvaluationRequired(order.getCountry())) {
            draftBuilder.additionalEvalRequests(Collections.singletonList(EAdditionalEvalRequests.SCA_EVALUATION));
        }
        if (configReader.isRecommendationOnly(order.getCountry())) {
            draftBuilder.coverageRequests(Collections.singletonList(CoverageRequests.NONE.name()));
        }
        if (ExecutionMode.PASSIVE.equals(configReader.getExecutionMode())) {
            draftBuilder.tags(Collections.singletonList(SignifydApi.PASSIVE_MODE));
        }
        return draftBuilder.build();
    }

    private ExtensionResponse<OrderUpdateAction> sendRequest(Order order, Payment payment, Customer customer) {
        ObjectMapper objMapper = new ObjectMapper();
        try {
            DecisionResponse checkoutResponse = this.signifydClient
                    .checkouts(generateRequest(order, payment, customer));
            logger.info("PreAuth API Success: Order successfully sent to Signifyd");
            return generateResponse(checkoutResponse, order);
        } catch (Signifyd4xxException e) {
            SignifydError signifydError = new SignifydError();
            signifydError.setErrors(e.getResponse().getErrors());
            signifydError.setTraceId(e.getResponse().getTraceId());
            ExtensionResponse<OrderUpdateAction> result = new ExtensionResponse<OrderUpdateAction>();
            try {
                result.addAction(
                        OrderSetCustomFieldActionBuilder.of()
                                .name(CustomFields.SIGNIFYD_ERROR)
                                .value(objMapper.writeValueAsString(signifydError))
                                .build());
                result.addAction(
                        OrderSetCustomFieldActionBuilder.of()
                                .name(CustomFields.IS_SENT_TO_SIGNIFYD)
                                .value(false)
                                .build());
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            return result;
        } catch (Signifyd5xxException e) {
            ExtensionResponse<OrderUpdateAction> result = new ExtensionResponse<OrderUpdateAction>();
            result.addAction(
                    OrderSetCustomFieldActionBuilder.of()
                            .name(CustomFields.IS_SENT_TO_SIGNIFYD)
                            .value(false)
                            .build());

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ExtensionResponse<OrderUpdateAction> generateResponse(
            DecisionResponse decisionResponse,
            Order order) throws IOException {
        DecisionCommand command = null;
        switch (decisionResponse.getDecision().checkpointAction) {
            case ACCEPT:
                command = new AcceptCommand(configReader, commercetoolsClient, order, decisionResponse);
                break;
            case REJECT:
                if (ExecutionMode.ACTIVE.equals(configReader.getExecutionMode())
                        && configReader.getDecisionActions(order.getCountry()).getReject().getActionType()
                                .equals(ActionType.DO_NOT_CREATE_ORDER)) {
                    ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<>();
                    response.addError(ExtensionError.builder()
                            .code(InvalidInputError.INVALID_INPUT)
                            .message("FRAUD_REJECTION: Order is not generated. Fraud detection found problems.")
                            .build());
                    return response;
                }
                command = new RejectCommand(configReader, commercetoolsClient, order, decisionResponse);
                break;
            case HOLD:
                command = new HoldCommand(configReader, commercetoolsClient, order, decisionResponse);
                break;
            default:
                throw new IllegalArgumentException("Checkpoint is not recognized");
        }

        ExtensionResponse<OrderUpdateAction> response;
        if (ExecutionMode.ACTIVE.equals(configReader.getExecutionMode())) {
            response = new ExtensionResponse<>(command.generateOrderActions());
        } else {
            response = new ExtensionResponse<>(command.prepareStandardOrderActions());
        }

        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.CURRENT_PRICE)
                        .value(Money.builder()
                                .centAmount(order.getTotalPrice().getCentAmount())
                                .currencyCode(order.getTotalPrice().getCurrencyCode())
                                .build())
                        .build());
        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.IS_SENT_TO_SIGNIFYD)
                        .value(true)
                        .build());
        return response;
    }

    private boolean isOrderEligibleToProcess(Payment payment) {
        return this.configReader.getExcludedPaymentMethods().stream()
                .noneMatch(p -> p.equals(payment.getPaymentMethodInfo().getMethod()));
    }

    private boolean isOrderReadyForCheckoutApiCall(Order order) {
        return order.getCustom() != null && order.getCustom().getFields().values().get(CustomFields.ORDER_CHANNEL) != null;
    }

    private void validateExtensionRequest(ExtensionRequest<OrderReference> extensionRequest) {
        if (!extensionRequest.getAction().equals(ACTION_TYPE)) {
            throw new UnsupportedOperationException(
                    String.format("Only supported API Extension action is %s received %s action is not supported",
                            ACTION_TYPE, extensionRequest.getAction()));
        }
    }
}
