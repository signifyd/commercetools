package com.signifyd.ctconnector.test.functions;

import com.commercetools.api.models.order.*;
import com.signifyd.ctconnector.function.ProxyFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProxyFunctionTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private ProxyFunction proxyFunction;

    @Mock
    private CommercetoolsClient commercetoolsClient;

    @BeforeEach
    public void setup(){
        System.setOut(new PrintStream(outContent));

        proxyFunction = new ProxyFunction(commercetoolsClient);
    }

    @Test
    public void WHEN_RequestIsNotValid_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        request.setAction("invalid-action");

        proxyFunction.apply(request);
        assert (outContent.toString().contains("Resource is not valid to add return info."));
    }

    @Test
    public void WHEN_RequestActionTypeIsNotSupported_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource proxyResource = new ProxyResource();
        
        request.setAction("invalid-action");
        request.setResource(proxyResource);

        proxyFunction.apply(request);
        assert (outContent.toString().contains(String.format("Received request action (%s) is not supported", request.getAction())));
    }

    @Test
    public void AddReturnInfo_WHEN_ResourceIsNotValidToAddReturnInfo_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource resource = new ProxyResource();

        // resource.setCustomerId("12345");
        resource.setOrderId("12345");
        resource.setReturnInfo(ReturnInfoBuilder.of()
                .returnTrackingId("12345")
                .items(ReturnItemBuilder.of()
                        .lineItemReturnItemBuilder()
                        .id("12345")
                        .quantity(1L)
                        .shipmentState(ReturnShipmentState.RETURNED)
                        .paymentState(ReturnPaymentState.INITIAL)
                        .createdAt(ZonedDateTime.now())
                        .lastModifiedAt(ZonedDateTime.now())
                        .lineItemId("12345")
                        .build())
                .build());
        request.setResource(resource);
        request.setAction("add-return-info");

        proxyFunction.apply(request);
        assert (outContent.toString().contains("Add return info failed: returnInfo is mandatory.") ||
            outContent.toString().contains("Add return info failed: orderId is mandatory.") ||
            outContent.toString().contains("Add return info failed: customerId is mandatory."));
    }

    @Test
    public void AddReturnInfo_WHEN_OrderIsNotAvailable_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource resource = new ProxyResource();

        resource.setCustomerId("12345");
        resource.setOrderId("12345");
        resource.setReturnInfo(ReturnInfoBuilder.of()
                .returnTrackingId("12345")
                .items(ReturnItemBuilder.of()
                        .lineItemReturnItemBuilder()
                        .id("12345")
                        .quantity(1L)
                        .shipmentState(ReturnShipmentState.RETURNED)
                        .paymentState(ReturnPaymentState.INITIAL)
                        .createdAt(ZonedDateTime.now())
                        .lastModifiedAt(ZonedDateTime.now())
                        .lineItemId("12345")
                        .build())
                .build());
        request.setResource(resource);
        request.setAction("add-return-info");

        when(this.commercetoolsClient.getOrderById(resource.getOrderId())).thenThrow(new RuntimeException("Order not found."));

        proxyFunction.apply(request);
        assert (outContent.toString().contains(String.format("Return info add failed: Order with %s could not find.", resource.getOrderId())));
    }

    @Test
    public void AddReturnInfo_WHEN_OrderCustomerIdIsNotValid_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource resource = new ProxyResource();

        resource.setCustomerId("12345");
        resource.setOrderId("12345");
        resource.setReturnInfo(ReturnInfoBuilder.of()
                .returnTrackingId("12345")
                .items(ReturnItemBuilder.of()
                        .lineItemReturnItemBuilder()
                        .id("12345")
                        .quantity(1L)
                        .shipmentState(ReturnShipmentState.RETURNED)
                        .paymentState(ReturnPaymentState.INITIAL)
                        .createdAt(ZonedDateTime.now())
                        .lastModifiedAt(ZonedDateTime.now())
                        .lineItemId("12345")
                        .build())
                .build());
        request.setResource(resource);
        request.setAction("add-return-info");

        Order order = new OrderImpl();
        order.setId(resource.getOrderId());
        order.setCustomerId("qwerty");

        when(this.commercetoolsClient.getOrderById(resource.getOrderId())).thenReturn(order);

        proxyFunction.apply(request);
        assert (outContent.toString().contains(String.format("Return info add failed: Customer with %s can not acces this order.", resource.getCustomerId())));
    }

    @Test
    public void AddReturnInfo_WHEN_AddReturnInfoIsFailed_THEN_ReturnFail() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource resource = new ProxyResource();

        resource.setCustomerId("12345");
        resource.setOrderId("12345");
        resource.setReturnInfo(ReturnInfoBuilder.of()
                .returnTrackingId("12345")
                .items(ReturnItemBuilder.of()
                        .lineItemReturnItemBuilder()
                        .id("12345")
                        .quantity(1L)
                        .shipmentState(ReturnShipmentState.RETURNED)
                        .paymentState(ReturnPaymentState.INITIAL)
                        .createdAt(ZonedDateTime.now())
                        .lastModifiedAt(ZonedDateTime.now())
                        .lineItemId("12345")
                        .build())
                .build());
        request.setResource(resource);
        request.setAction("add-return-info");

        Order order = new OrderImpl();
        order.setId(resource.getOrderId());
        order.setCustomerId("12345");

        when(this.commercetoolsClient.getOrderById(resource.getOrderId())).thenReturn(order);

        when(this.commercetoolsClient.addReturnInfo(any(), any())).thenThrow(new RuntimeException("Any error"));
        assertThatThrownBy(() -> proxyFunction.apply(request)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void AddReturnInfo_WHEN_AddReturnInfoIsSuccess_THEN_ReturnSucces() {
        ProxyRequest<ProxyResource> request = new ProxyRequest<>();
        ProxyResource resource = new ProxyResource();

        resource.setCustomerId("12345");
        resource.setOrderId("12345");
        resource.setReturnInfo(ReturnInfoBuilder.of()
                .returnTrackingId("12345")
                .items(ReturnItemBuilder.of()
                        .lineItemReturnItemBuilder()
                        .id("12345")
                        .quantity(1L)
                        .shipmentState(ReturnShipmentState.RETURNED)
                        .paymentState(ReturnPaymentState.INITIAL)
                        .createdAt(ZonedDateTime.now())
                        .lastModifiedAt(ZonedDateTime.now())
                        .lineItemId("12345")
                        .build())
                .build());
        request.setResource(resource);
        request.setAction("add-return-info");

        Order order = new OrderImpl();
        order.setId(resource.getOrderId());
        order.setCustomerId("12345");

        when(this.commercetoolsClient.getOrderById(resource.getOrderId())).thenReturn(order);

        proxyFunction.apply(request);
        assert (outContent.toString().contains("Return info add succeed."));
    }
}