package com.signifyd.ctconnector.test.functions;

import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.CustomFieldsBuilder;
import com.commercetools.api.models.type.CustomFieldsImpl;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.FieldContainerBuilder;
import com.commercetools.api.models.type.FieldContainerImpl;
import com.commercetools.api.models.type.TypeReferenceBuilder;
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.ReturnInfoTransition;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ReturnFunctionTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private ReturnFunction returnFunction;

    @Mock
    private ConfigReader configReader;
    @Mock
    private CommercetoolsClient commercetoolsClient;

    @Mock
    private SignifydClient signifydClient;

    @Mock
    private SignifydMapper signifydMapper;

    @BeforeEach
    public void setup(){
        System.setOut(new PrintStream(outContent));

        returnFunction = new ReturnFunction(
                configReader,
                signifydClient,
                signifydMapper);
    }

    @Test
    public void WHEN_IsSentToSignifydFalseOrNull_THEN_ReturnErrorResponse() {
        OrderReferenceImpl resource = new OrderReferenceImpl();
        OrderImpl order = new OrderImpl();

        resource.setObj(order);

        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setResource(resource);

        returnFunction.apply(request);
        assert (outContent.toString().contains("Order was not sent to Signifyd. The process cannot go on."));
    }

    @Test
    public void Attempt_WHEN_isReturnTrackingIdNotValid_THEN_ReturnErrorResponse() {
        OrderReferenceImpl resource = new OrderReferenceImpl();
        OrderImpl order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        ReturnInfoImpl returnInfo = new ReturnInfoImpl();
        returnInfo.setItems(new ReturnItemImpl());
        returnInfo.setReturnTrackingId(null);

        order.setReturnInfo(returnInfo);

        resource.setObj(order);
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setResource(resource);

        returnFunction.apply(request);
        assert (outContent.toString().contains("Return tracking id should be unique and not empty."));
    }

    @SneakyThrows
    @Test
    public void Attempt_WHEN_AttemptReturn_THEN_ReturnSuccessResponse() {
        OrderReferenceImpl resource = new OrderReferenceImpl();
        OrderImpl order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();

        order.setId("12345");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        ReturnInfoImpl returnInfo = new ReturnInfoImpl();
        List<ReturnItem> returnItems = new ArrayList<>();
        returnItems.add(
            ReturnItemBuilder.of().lineItemReturnItemBuilder()
                .shipmentState(ReturnShipmentState.RETURNED)
                .paymentState(ReturnPaymentState.INITIAL)
                .quantity(1L)
                .id("12345")
                .lineItemId("qwerty")
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .build()
        );
        returnInfo.setItems(returnItems);
        returnInfo.setReturnTrackingId("abc123");

        order.setReturnInfo(returnInfo);

        resource.setObj(order);
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setResource(resource);

        AttemptReturnResponse attemptReturnResponse = new AttemptReturnResponse();
        Decision decision = new Decision();
        decision.checkpointAction = Action.ACCEPT;
        attemptReturnResponse.setDecision(decision);

        when(this.signifydClient.attemptReturn(any())).thenReturn(attemptReturnResponse);

        ExtensionResponse<OrderUpdateAction> response = returnFunction.apply(request);
        assertThat(response.getErrors()).isNull();
    }


    @SneakyThrows
    @Test
    public void Execute_WHEN_ExecuteReturn_THEN_ReturnSuccessResponse() {
        OrderReferenceImpl resource = new OrderReferenceImpl();
        OrderImpl order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();

        order.setId("12345");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        ReturnInfoImpl returnInfo = new ReturnInfoImpl();
        List<ReturnItem> returnItems = new ArrayList<>();
        returnItems.add(
            ReturnItemBuilder.of().lineItemReturnItemBuilder()
                .shipmentState(ReturnShipmentState.RETURNED)
                .paymentState(ReturnPaymentState.INITIAL)
                .quantity(1L)
                .id("12345")
                .lineItemId("qwerty")
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .shipmentState(ReturnShipmentState.BACK_IN_STOCK)
                .custom(
                    CustomFieldsBuilder.of()
                        .fields(
                            FieldContainerBuilder.of()
                                .addValue(com.signifyd.ctconnector.function.constants.CustomFields.RETURN_ITEM_TRANSITION,
                                    ReturnInfoTransition.EXECUTE.name()).build())
                        .type(TypeReferenceBuilder.of().id(com.signifyd.ctconnector.function.constants.CustomFields.SIGNIFYD_RETURN_ITEM_TYPE).build())
                        .build())
                .build()
        );
        returnInfo.setItems(returnItems);
        returnInfo.setReturnTrackingId("abc123");

        order.setReturnInfo(returnInfo);

        resource.setObj(order);
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setResource(resource);

        ExecuteReturnResponse executeReturnResponse = new ExecuteReturnResponse();
        when(this.signifydClient.executeReturn(any())).thenReturn(executeReturnResponse);

        ExtensionResponse<OrderUpdateAction> response = returnFunction.apply(request);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    public void WHEN_SignifydResponse4xxOr5xxOrIOException_THEN_ThrowRuntimeException() {
        OrderReferenceImpl resource = new OrderReferenceImpl();
        OrderImpl order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        resource.setObj(order);
        ExtensionRequest<OrderReference> request = new ExtensionRequest<>();
        request.setResource(resource);
        // then
        assertThatThrownBy(() -> returnFunction.apply(request)).isInstanceOf(RuntimeException.class);
    }
}