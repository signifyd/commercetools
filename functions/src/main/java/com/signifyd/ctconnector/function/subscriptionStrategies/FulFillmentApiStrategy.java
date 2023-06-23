package com.signifyd.ctconnector.function.subscriptionStrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.DeliveryAddedMessage;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.Delivery;
import com.commercetools.api.models.order.DeliveryItem;
import com.commercetools.api.models.order.Order;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Address;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.Fulfillment;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.FulfillmentDestination;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.FulfillmentProduct;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment.FulfillmentRequestDraft;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

public class FulFillmentApiStrategy implements SubscriptionStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public FulFillmentApiStrategy(
        CommercetoolsClient commercetoolsClient,
        SignifydClient signifydClient,
        ConfigReader configReader
    ) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.configReader = configReader;
    }

    @Override
    public void execute(Message message) {
        Order order = this.commercetoolsClient.getOrderById(message.getResource().getId());
        OrderHelper.controlOrderSentToSignifyd(order);
        List<Fulfillment> fulfillments = null;
        switch (message.getType()) {
            case DeliveryAddedMessage.DELIVERY_ADDED:
                DeliveryAddedMessage deliveryAddedMessage = (DeliveryAddedMessage) message;
                fulfillments = mapFulfillmentsFromCommercetools(order, deliveryAddedMessage.getDelivery());
                break;
            default:
                throw new IllegalArgumentException();
        }

        FulfillmentRequestDraft draft = FulfillmentRequestDraft
                .builder()
                .orderId(order.getId())
                .fulfillments(fulfillments)
                .build();

        executeWithErrorHandling(draft);
    }

    private void executeWithErrorHandling(FulfillmentRequestDraft draft) {
        try {
            this.signifydClient.fulfillment(draft);
            logger.info("Fulfillment API Success : New or updated fulfillment sent to Signifyd");
        } catch (Signifyd4xxException | Signifyd5xxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Fulfillment> mapFulfillmentsFromCommercetools(Order order, Delivery delivery) {
        List<Fulfillment> fulfillments = new ArrayList<>();
        List<DeliveryItem> deliveryItems = new ArrayList<>();
        if (delivery.getItems() == null || delivery.getItems().isEmpty()) {
            deliveryItems = delivery.getParcels().stream().flatMap(p -> p.getItems().stream()).collect(Collectors.toList());
        } else {
            deliveryItems = delivery.getItems();
        }
        List<FulfillmentProduct> fulfillmentProducts = deliveryItems.stream().map(di -> {
            var lineItem = order.getLineItems().stream().filter(li -> li.getId().equals(di.getId())).findFirst().orElseThrow();
            return FulfillmentProduct.builder()
                    .itemId(lineItem.getProductId())
                    .itemName(lineItem.getName().get(configReader.getLocale()))
                    .itemQuantity(lineItem.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        List<String> trackingNumbers = delivery.getParcels().stream().map(p -> p.getTrackingData().getTrackingId()).collect(Collectors.toList());
        String carrier = delivery.getParcels().size() > 0 ? delivery.getParcels().get(0).getTrackingData().getCarrier() : "";
        var address = delivery.getAddress();
        fulfillments.add(
                Fulfillment.builder()
                        .shipmentId(delivery.getId())
                        .trackingNumbers(trackingNumbers)
                        .carrier(carrier)
                        .products(fulfillmentProducts)
                        .destination(
                                FulfillmentDestination
                                        .builder()
                                        .fullName(String.format("%s %s", address.getFirstName(),
                                                address.getLastName()))
                                        .confirmationPhone(address.getPhone())
                                        .address(
                                                Address
                                                        .builder()
                                                        .streetAddress(address.getStreetName())
                                                        .unit(address.getAdditionalStreetInfo())
                                                        .city(address.getCity())
                                                        .postalCode(address.getPostalCode())
                                                        .countryCode(address.getCountry())
                                                        .provinceCode(address.getCountry())
                                                        .build())
                                        .build())
                        .build());

        return fulfillments;
    }
}
