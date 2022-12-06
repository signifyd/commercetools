package com.signifyd.ctconnector.test.functions;

import com.commercetools.api.models.common.TypedMoneyImpl;
import com.commercetools.api.models.customer.CustomerImpl;
import com.commercetools.api.models.order.*;
import com.commercetools.api.models.payment.PaymentImpl;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.payment.PaymentReferenceImpl;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.CustomFieldsImpl;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.FieldContainerImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ErrorResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.ScaEvaluation;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.config.model.ActionType;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfig;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfigs;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PreAuthFunctionTest {

    private PreAuthFunction preAuthFunction;

    private static ObjectMapper objectMapper;
    @Mock
    private ConfigReader configReader;
    @Mock
    private CommercetoolsClient commercetoolsClient;

    @Mock
    private SignifydClient signifydClient;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private SignifydMapper signifydMapper;

    @BeforeAll
    public static void setup(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    @BeforeEach
    public void setupEach() {
        preAuthFunction = new PreAuthFunction(
                configReader,
                commercetoolsClient,
                signifydClient,
                propertyReader,
                signifydMapper);
    }

    @Test
    public void WHEN_ActionTypeIsNotCreate_THEN_ThrowUnsupportedOperationException() {
        // given
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setAction("NotCreate");
        // then
        assertThatThrownBy(() -> preAuthFunction.apply(request)).isInstanceOf(UnsupportedOperationException.class);
    }

    @SneakyThrows
    @Test
    public void WHEN_SignifydResponse4XX_THEN_ReturnWithMissingData() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd4xxException exception = new Signifyd4xxException(errorResponse, 400);
        when(this.signifydClient.checkouts(any())).thenThrow(exception);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_SignifydResponse5XX_THEN_ReturnWithMissingData() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        when(this.configReader.isScaEvaluationRequired(order.getCountry())).thenReturn(true);
        when(this.configReader.isRecommendationOnly(order.getCountry())).thenReturn(true);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd5xxException exception = new Signifyd5xxException(errorResponse, 500);
        when(this.signifydClient.checkouts(any())).thenThrow(exception);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ActionTypeIsNone_THEN_ReturnSuccess() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = " {\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    }\n" +
                "}";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(ActionType.NONE);
        decisionActionConfigs.setAccept(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ActionTypeIsDefaultStateTransition_THEN_ReturnSuccess() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(ActionType.DEFAULT_STATE_TRANSITION);
        decisionActionConfigs.setAccept(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ActionTypeIsCustomStateTransition_THEN_ReturnSuccess() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(ActionType.CUSTOM_STATE_TRANSITION);
        decisionActionConfigs.setAccept(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ActionTypeIsNotCovered_THENTHROW_IllegalArgumentException() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(null);
        decisionActionConfigs.setAccept(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then
        assertThatThrownBy(() -> preAuthFunction.apply(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ExecutionTypeIsNotActive_THEN_ReturnSuccess() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.PASSIVE);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsReject_AND_ExecutionTypeIsActive_AND_ActionTypeIsDoNotCreateOrder_THEN_ReturnSuccess() {
        // given
        PaymentReference paymentReference = new PaymentReferenceImpl();
        ArrayList<PaymentReference> payments = new ArrayList<>();
        PaymentInfo paymentInfo = new PaymentInfoImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fieldContainer = new FieldContainerImpl();
        OrderImpl order = new OrderImpl();
        OrderReferenceImpl resource = new OrderReferenceImpl();
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        paymentReference.setId("paymentReferenceId");
        payments.add(paymentReference);
        paymentInfo.setPayments(payments);
        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
        customFields.setFields(fieldContainer);
        order.setPaymentInfo(paymentInfo);
        order.setCustomerId("orderCustomerId");
        order.setCustom(customFields);
        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
        totalPrice.setCentAmount(10000L);
        totalPrice.setCurrencyCode("USD");
        order.setTotalPrice(totalPrice);
        resource.setObj(order);
        request.setResource(resource);
        request.setAction("Create");
        // when
        PaymentImpl payment = new PaymentImpl();
        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
        CustomerImpl customer = new CustomerImpl();
        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.REJECT;
        decisionResponse.setDecision(decision);
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(ActionType.DO_NOT_CREATE_ORDER);
        decisionActionConfigs.setReject(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then
        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
        assertThat(response).isNotNull();
    }

//    @SneakyThrows
//    @Test
//    public void WHEN_CheckpointActionIsReject_AND_ExecutionTypeIsActive_AND_ActionTypeIsNotDoNotCreateOrder_THEN_ReturnSuccess() {
//        // given
//        PaymentReference paymentReference = new PaymentReferenceImpl();
//        ArrayList<PaymentReference> payments = new ArrayList<>();
//        PaymentInfo paymentInfo = new PaymentInfoImpl();
//        CustomFields customFields = new CustomFieldsImpl();
//        FieldContainer fieldContainer = new FieldContainerImpl();
//        OrderImpl order = new OrderImpl();
//        OrderReferenceImpl resource = new OrderReferenceImpl();
//        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
//        paymentReference.setId("paymentReferenceId");
//        payments.add(paymentReference);
//        paymentInfo.setPayments(payments);
//        fieldContainer.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "checkoutId");
//        customFields.setFields(fieldContainer);
//        order.setPaymentInfo(paymentInfo);
//        order.setCustomerId("orderCustomerId");
//        order.setCustom(customFields);
//        TypedMoneyImpl totalPrice = new TypedMoneyImpl();
//        totalPrice.setCentAmount(10000L);
//        totalPrice.setCurrencyCode("USD");
//        order.setTotalPrice(totalPrice);
//        resource.setObj(order);
//        request.setResource(resource);
//        request.setAction("Create");
//        // when
//        PaymentImpl payment = new PaymentImpl();
//        when(this.commercetoolsClient.getPaymentById(paymentReference.getId())).thenReturn(payment);
//        CustomerImpl customer = new CustomerImpl();
//        when(this.commercetoolsClient.getCustomerById(order.getCustomerId())).thenReturn(customer);
//        DecisionResponse decisionResponse = new DecisionResponse();
//        Decision decision = new Decision();
//        decision.checkpointAction = Action.REJECT;
//        decisionResponse.setDecision(decision);
//        String scaEvaluationJson = "{\n" +
//                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
//                "    \"exemptionDetails\": {\n" +
//                "      \"exemption\": \"TRA\",\n" +
//                "      \"placement\": \"AUTHORIZATION\"\n" +
//                "    },\n" +
//                "    \"exclusionDetails\":{\n" +
//                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
//                "    }\n" +
//                "  }";
//        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
//        decisionResponse.setScaEvaluation(scaEvaluation);
//        when(this.signifydClient.checkouts(any())).thenReturn(decisionResponse);
//        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
//        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
//        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
//        decisionActionConfig.setActionType(ActionType.NONE);
//        decisionActionConfigs.setReject(decisionActionConfig);
//        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
//        // then
//        ExtensionResponse<OrderUpdateAction> response = preAuthFunction.apply(request);
//        assertThat(response).isNotNull();
//    }
}
