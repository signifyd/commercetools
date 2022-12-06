package com.signifyd.ctconnector.test.functions;

import com.commercetools.api.models.cart.LineItem;
import com.commercetools.api.models.cart.LineItemImpl;
import com.commercetools.api.models.cart.ShippingInfo;
import com.commercetools.api.models.cart.ShippingInfoImpl;
import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.common.Reference;
import com.commercetools.api.models.common.ReferenceImpl;
import com.commercetools.api.models.common.TypedMoneyBuilder;
import com.commercetools.api.models.order.Delivery;
import com.commercetools.api.models.order.DeliveryImpl;
import com.commercetools.api.models.order.DeliveryItem;
import com.commercetools.api.models.order.DeliveryItemImpl;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderImpl;
import com.commercetools.api.models.order.Parcel;
import com.commercetools.api.models.order.ParcelImpl;
import com.commercetools.api.models.order.TrackingData;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentImpl;
import com.commercetools.api.models.payment.Transaction;
import com.commercetools.api.models.payment.TransactionImpl;
import com.commercetools.api.models.state.StateReference;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.CustomFieldsImpl;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.FieldContainerImpl;
import com.commercetools.api.models.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ErrorResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;

import lombok.SneakyThrows;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionFunctionTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private SubscriptionFunction subscriptionFunction;

    @Mock
    private ConfigReader configReader;
    @Mock
    private CommercetoolsClient commercetoolsClient;
    @Mock
    private SignifydClient signifydClient;

    private static ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        subscriptionFunction = new SubscriptionFunction(
                configReader,
                commercetoolsClient,
                signifydClient);
    }

    @AfterEach
    public void after() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void WHEN_MessageTypeIsNotSupported_THEN_ThrowNotImplementedException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // create a message unsopported by subscription functions
        Message message = Message.orderStateTransitionBuilder()
            .id("f533548a-414c-44fb-8035-e060c77f8f86")
            .version(1L)
            .createdAt(ZonedDateTime.now())
            .lastModifiedAt(ZonedDateTime.now())
            .sequenceNumber(1L)
            .resource(resource)
            .resourceVersion(1L)
            .state(StateReference.builder().id("test-id").build())
            .force(false)
            .build();

        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(NotImplementedException.class);
    }

    @Test
    public void FulFillment_WHEN_IsSentToSignifydFalseOrNull_THEN_ThrowRuntimeException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Delivery delivery = new DeliveryImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // create message
        Message message = Message.deliveryAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .delivery(delivery)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void FulFillment_WHEN_IsDeliveryItemsNullOrEmpty_THEN_Success() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        Delivery delivery = new DeliveryImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        lineItem.setProductId("ae5e9862-76de-4f26-b12e-c63c19184eaa");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // Create parcels and deliveries to set into delivery
        List<DeliveryItem> deliveryItems = new ArrayList<>();
        List<Parcel> parcels = new ArrayList<>();

        DeliveryItem deliveryItem = new DeliveryItemImpl();
        deliveryItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Parcel parcel = new ParcelImpl();

        deliveryItems.add(deliveryItem);
        parcel.setItems(deliveryItems);
        parcel.setTrackingData(
                TrackingData.builder()
                        .carrier("UPS")
                        .trackingId("12345")
                        .build());

        parcels.add(parcel);

        delivery.setParcels(parcels);
        delivery.setAddress(
                Address.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phone("+1999886565")
                        .streetName("new york")
                        .additionalStreetInfo("916. sreet")
                        .city("New York")
                        .postalCode("916")
                        .country("US")
                        .build());

        // create message
        Message message = Message.deliveryAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .delivery(delivery)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");
        var response = subscriptionFunction.apply(message);
        assertThat(response).isNull();
    }

    @Test
    public void FulFillment_WHEN_IsDeliveryItemsNotPresentedLineItems_THEN_ThrowNoSuchElementException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        Delivery delivery = new DeliveryImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        lineItem.setProductId("ae5e9862-76de-4f26-b12e-c63c19184eaa");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // Create parcels and deliveries to set into delivery
        List<DeliveryItem> deliveryItems = new ArrayList<>();
        List<Parcel> parcels = new ArrayList<>();

        DeliveryItem deliveryItem = new DeliveryItemImpl();
        deliveryItem.setId("ffffffff-gggg-hhhh-iiii-jjjjjjjjjjjj");
        Parcel parcel = new ParcelImpl();

        deliveryItems.add(deliveryItem);
        parcel.setItems(deliveryItems);
        
        parcels.add(parcel);

        delivery.setParcels(parcels);

        // create message
        Message message = Message.deliveryAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .delivery(delivery)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void FulFillment_WHEN_MapFulfillmentsFromCommercetools_THEN_ThrowSignifyd4xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        Delivery delivery = new DeliveryImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        lineItem.setProductId("ae5e9862-76de-4f26-b12e-c63c19184eaa");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // Create parcels and deliveries to set into delivery
        List<DeliveryItem> deliveryItems = new ArrayList<>();
        List<Parcel> parcels = new ArrayList<>();

        DeliveryItem deliveryItem = new DeliveryItemImpl();
        deliveryItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Parcel parcel = new ParcelImpl();

        deliveryItems.add(deliveryItem);
        parcel.setItems(deliveryItems);
        parcel.setTrackingData(
                TrackingData.builder()
                        .carrier("UPS")
                        .trackingId("12345")
                        .build());

        parcels.add(parcel);

        delivery.setParcels(parcels);
        delivery.setAddress(
                Address.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phone("+1999886565")
                        .streetName("new york")
                        .additionalStreetInfo("916. sreet")
                        .city("New York")
                        .postalCode("916")
                        .country("US")
                        .build());

        // create message
        Message message = Message.deliveryAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .delivery(delivery)
                .build();


        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd4xxException exception = new Signifyd4xxException(errorResponse, 400);

        when(this.signifydClient.fulfillment(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void FulFillment_WHEN_MapFulfillmentsFromCommercetools_THEN_ThrowSignifyd5xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        Delivery delivery = new DeliveryImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        lineItem.setProductId("ae5e9862-76de-4f26-b12e-c63c19184eaa");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // Create parcels and deliveries to set into delivery
        List<DeliveryItem> deliveryItems = new ArrayList<>();
        List<Parcel> parcels = new ArrayList<>();

        DeliveryItem deliveryItem = new DeliveryItemImpl();
        deliveryItem.setId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Parcel parcel = new ParcelImpl();

        deliveryItems.add(deliveryItem);
        parcel.setItems(deliveryItems);
        parcel.setTrackingData(
                TrackingData.builder()
                        .carrier("UPS")
                        .trackingId("12345")
                        .build());

        parcels.add(parcel);

        delivery.setParcels(parcels);
        delivery.setAddress(
                Address.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .phone("+1999886565")
                        .streetName("new york")
                        .additionalStreetInfo("916. sreet")
                        .city("New York")
                        .postalCode("916")
                        .country("US")
                        .build());

        // create message
        Message message = Message.deliveryAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .delivery(delivery)
                .build();


        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd5xxException exception = new Signifyd5xxException(errorResponse, 500);

        when(this.signifydClient.fulfillment(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void Reprice_WHEN_IsSentToSignifydFalseOrNull_THEN_ThrowRuntimeException() {
        // Initializing
        LineItem lineItem = new LineItemImpl();
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // create a new line item
        lineItem.setId("e6e6026c-8594-4e7f-a01e-035e1f9fa377");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(50000L)
                        .fractionDigits(2)
                        .build());
        lineItem.setQuantity(1L);

        // create message
        Message message = Message.orderLineItemAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .lineItem(lineItem)
                .addedQuantity(1L)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void Reprice_WHEN_TotalPriceEqualsCurrentPrice_THEN_CanNotContinue() {
        // Initializing
        LineItem lineItem = new LineItemImpl();
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue("currentPrice",
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(110000L)
                        .fractionDigits(2)
                        .build());
        customFields.setFields(fields);
        order.setCustom(customFields);

        // set order total price
        order.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(110000L)
                        .fractionDigits(2)
                        .build());

        // create a new line item
        lineItem.setId("e6e6026c-8594-4e7f-a01e-035e1f9fa377");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(50000L)
                        .fractionDigits(2)
                        .build());
        lineItem.setQuantity(1L);

        // create message
        Message message = Message.orderLineItemAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .lineItem(lineItem)
                .addedQuantity(1L)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        subscriptionFunction.apply(message);
        assert (outContent.toString().contains("Order sent to Signifyd failed"));
    }

    @Test
    @SneakyThrows
    public void Reprice_WHEN_TotalPriceNotEqualsCurrentPrice_THEN_ThrowSignifyd4xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        ShippingInfo shippingInfo = new ShippingInfoImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue("currentPrice",
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(110000L)
                        .fractionDigits(2)
                        .build());
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("e6e6026c-8594-4e7f-a01e-035e1f9fa377");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(50000L)
                        .fractionDigits(2)
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // set order shipping info
        shippingInfo.setPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(5000L)
                        .fractionDigits(2)
                        .build());
        order.setShippingInfo(shippingInfo);

        // set order total price
        order.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(165000L)
                        .fractionDigits(2)
                        .build());

        // create message
        Message message = Message.orderLineItemAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .lineItem(lineItem)
                .addedQuantity(1L)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd4xxException exception = new Signifyd4xxException(errorResponse, 400);

        when(this.signifydClient.reprice(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Reprice_WHEN_TotalPriceNotEqualsCurrentPrice_THEN_ThrowSignifyd5xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        ShippingInfo shippingInfo = new ShippingInfoImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue("currentPrice",
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(110000L)
                        .fractionDigits(2)
                        .build());
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("e6e6026c-8594-4e7f-a01e-035e1f9fa377");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(50000L)
                        .fractionDigits(2)
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // set order shipping info
        shippingInfo.setPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(5000L)
                        .fractionDigits(2)
                        .build());
        order.setShippingInfo(shippingInfo);

        // set order total price
        order.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(165000L)
                        .fractionDigits(2)
                        .build());

        // create message
        Message message = Message.orderLineItemAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .lineItem(lineItem)
                .addedQuantity(1L)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd5xxException exception = new Signifyd5xxException(errorResponse, 500);

        when(this.signifydClient.reprice(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Reprice_WHEN_TotalPriceNotEqualsCurrentPrice_THEN_Success() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        List<LineItem> lineItems = new ArrayList<>();
        LineItem lineItem = new LineItemImpl();
        ShippingInfo shippingInfo = new ShippingInfoImpl();
        resource.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue("currentPrice",
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(110000L)
                        .fractionDigits(2)
                        .build());
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create a new line item
        lineItem.setId("e6e6026c-8594-4e7f-a01e-035e1f9fa377");
        lineItem.setName(
                LocalizedString.builder()
                        .addValue("en", "Pumps ”Flex” Michael Kors")
                        .build());
        lineItem.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(50000L)
                        .fractionDigits(2)
                        .build());
        lineItem.setQuantity(1L);

        // set order line items
        lineItems.add(lineItem);
        order.setLineItems(lineItems);

        // set order shipping info
        shippingInfo.setPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(5000L)
                        .fractionDigits(2)
                        .build());
        order.setShippingInfo(shippingInfo);

        // set order total price
        order.setTotalPrice(
                TypedMoneyBuilder.of().centPrecisionBuilder()
                        .currencyCode("EUR")
                        .centAmount(165000L)
                        .fractionDigits(2)
                        .build());

        // create message
        Message message = Message.orderLineItemAddedBuilder()
                .id("f533548a-414c-44fb-8035-e060c77f8f86")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .lineItem(lineItem)
                .addedQuantity(1L)
                .build();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        when(this.configReader.getLocale()).thenReturn("en");
        when(this.commercetoolsClient.setCustomFields(any(), any())).thenReturn(order);

        subscriptionFunction.apply(message);
        assert (outContent.toString().contains("Reprice API Success"));
    }

    @Test
    public void Transaction_WHEN_ConfigIsNotPreAuth_THEN_Return() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Transaction transaction = new TransactionImpl();
        resource.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setCountry("US");

        // create message
        Message message = Message.paymentTransactionAddedBuilder()
                .id("56e913a1-9d75-42d4-bd33-4e9a02cb6905")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .transaction(transaction)
                .build();

        when(this.commercetoolsClient.getOrderByPaymentId(message.getResource().getId())).thenReturn(order);
        when(this.configReader.isPreAuth(order.getCountry())).thenReturn(false);
        assertThat(subscriptionFunction.apply(message)).isNull();
    }

    @Test
    public void Transaction_WHEN_IsSentToSignifydFalseOrNull_THEN_ThrowRuntimeException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Transaction transaction = new TransactionImpl();
        resource.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setCountry("US");

        // create message
        Message message = Message.paymentTransactionAddedBuilder()
                .id("56e913a1-9d75-42d4-bd33-4e9a02cb6905")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .transaction(transaction)
                .build();

        when(this.commercetoolsClient.getOrderByPaymentId(message.getResource().getId())).thenReturn(order);
        when(this.configReader.isPreAuth(order.getCountry())).thenReturn(true);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Transaction_WHEN_CreateTransactionRequestDraft_THEN_ThrowSignifyd4xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Payment payment = new PaymentImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        Transaction transaction = new TransactionImpl();
        resource.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setCountry("US");
        payment.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "93e6dc4e-14df-4774-8320-04163636193c");
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create message
        Message message = Message.paymentTransactionAddedBuilder()
                .id("56e913a1-9d75-42d4-bd33-4e9a02cb6905")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .transaction(transaction)
                .build();

        when(this.commercetoolsClient.getOrderByPaymentId(message.getResource().getId())).thenReturn(order);
        when(this.commercetoolsClient.getPaymentById(any())).thenReturn(payment);
        when(this.configReader.isPreAuth(order.getCountry())).thenReturn(true);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd4xxException exception = new Signifyd4xxException(errorResponse, 400);

        when(this.signifydClient.transaction(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Transaction_WHEN_CreateTransactionRequestDraft_THEN_ThrowSignifyd5xxException() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Payment payment = new PaymentImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        Transaction transaction = new TransactionImpl();
        resource.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setCountry("US");
        payment.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "93e6dc4e-14df-4774-8320-04163636193c");
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create message
        Message message = Message.paymentTransactionAddedBuilder()
                .id("56e913a1-9d75-42d4-bd33-4e9a02cb6905")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .transaction(transaction)
                .build();

        when(this.commercetoolsClient.getOrderByPaymentId(message.getResource().getId())).thenReturn(order);
        when(this.commercetoolsClient.getPaymentById(any())).thenReturn(payment);
        when(this.configReader.isPreAuth(order.getCountry())).thenReturn(true);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message"});
        errorResponse.setTraceId("Error traceId");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd5xxException exception = new Signifyd5xxException(errorResponse, 500);

        when(this.signifydClient.transaction(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Transaction_WHEN_CreateTransactionRequestDraft_THEN_Success() {
        // Initializing
        Reference resource = new ReferenceImpl();
        Order order = new OrderImpl();
        Payment payment = new PaymentImpl();
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        Transaction transaction = new TransactionImpl();
        resource.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");
        order.setId("6fc5a063-1d20-4122-aed5-18df152ab2a9");
        order.setCountry("US");
        payment.setId("759864ad-60cc-45e3-9bae-e1de3e685ffd");

        // set order custom fields
        fields.setValue("isSentToSignifyd", true);
        fields.setValue(com.signifyd.ctconnector.function.constants.CustomFields.CHECKOUT_ID, "93e6dc4e-14df-4774-8320-04163636193c");
        customFields.setFields(fields);
        order.setCustom(customFields);

        // create message
        Message message = Message.paymentTransactionAddedBuilder()
                .id("56e913a1-9d75-42d4-bd33-4e9a02cb6905")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .transaction(transaction)
                .build();

        when(this.commercetoolsClient.getOrderByPaymentId(message.getResource().getId())).thenReturn(order);
        when(this.commercetoolsClient.getPaymentById(any())).thenReturn(payment);
        when(this.configReader.isPreAuth(order.getCountry())).thenReturn(true);

        subscriptionFunction.apply(message);
        assert (outContent.toString().contains("Transaction API Success: Transactions Update sent to Signifyd"));
    }

    @Test
    @SneakyThrows
    public void Reroute_WHEN_IsSentToSignifydFalseOrNull_THEN_ThrowRuntimeException() {
        Order order = this.createOrderForReroute(false);
        Message message = this.createMessageForReroute();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Reroute_WHEN_OrderRerouted_THEN_Success() {
        Order order = this.createOrderForReroute(true);
        Message message = this.createMessageForReroute();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);
        subscriptionFunction.apply(message);
        assert ("Reroute API Success : Delivery Address Change sent to Signifyd\n")
                .equals(outContent.toString());
    }
    @Test
    @SneakyThrows
    public void Reroute_WHEN_OrderNotRerouted_THEN_ThrowSignifyd4xxException() {
        Order order = this.createOrderForReroute(true);
        Message message = this.createMessageForReroute();

        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message for Reroute 4XX"});
        errorResponse.setTraceId("1111400");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd4xxException exception = new Signifyd4xxException(errorResponse, 400);

        when(this.signifydClient.reroute(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void Reroute_WHEN_OrderNotRerouted_THEN_ThrowSignifyd5xxException() {
        Order order = this.createOrderForReroute(true);
        Message message = this.createMessageForReroute();
        when(this.commercetoolsClient.getOrderById(message.getResource().getId())).thenReturn(order);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessages(new String[]{"Error test message for Reroute 5XX"});
        errorResponse.setTraceId("11111500");
        errorResponse.setErrors(Map.of("error", new String[]{"error1", "error2"}));
        Signifyd5xxException exception = new Signifyd5xxException(errorResponse, 500);

        when(this.signifydClient.reroute(any())).thenThrow(exception);
        assertThatThrownBy(() -> subscriptionFunction.apply(message)).isInstanceOf(RuntimeException.class);
    }

    private Order createOrderForReroute(boolean isSentToSignifyd) throws JsonProcessingException {
        Order order = new OrderImpl();
        //set order id and shipping address
        order.setId("571bcf99-b011-4a14-85c0-9da65d516a7a");
        String shippingAddressJson = "{\n" +
                "        \"firstName\": \"Furkan\",\n" +
                "        \"lastName\": \"Sahin\",\n" +
                "        \"streetName\": \"Konak, Guzelyali Mahallesi\",\n" +
                "        \"additionalStreetInfo\": \"Meteoroloji\",\n" +
                "        \"postalCode\": \"35000\",\n" +
                "        \"city\": \"Izmir\",\n" +
                "        \"country\": \"DE\",\n" +
                "        \"email\": \"test@reroute.com\"\n" +
                "    }";
        Address shippingAddress = objectMapper.readValue(shippingAddressJson, Address.class);
        order.setShippingAddress(shippingAddress);

        // set order custom fields
        CustomFields customFields = new CustomFieldsImpl();
        FieldContainer fields = new FieldContainerImpl();
        fields.setValue("isSentToSignifyd", isSentToSignifyd);
        customFields.setFields(fields);
        order.setCustom(customFields);

        return order;
    }

    private Message createMessageForReroute() {
        Reference resource = new ReferenceImpl();
        resource.setId("571bcf99-b011-4a14-85c0-9da65d516a7a");

        Message message = Message.orderShippingAddressSetBuilder()
                .id("69fb86a9-4bce-4533-95c6-298a4289fa8d")
                .version(1L)
                .createdAt(ZonedDateTime.now())
                .lastModifiedAt(ZonedDateTime.now())
                .sequenceNumber(1L)
                .resource(resource)
                .resourceVersion(1L)
                .build();

        return message;
    }
}
