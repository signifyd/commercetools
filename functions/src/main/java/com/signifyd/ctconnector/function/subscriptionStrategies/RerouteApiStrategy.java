package com.signifyd.ctconnector.function.subscriptionStrategies;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.Order;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Address;
import com.signifyd.ctconnector.function.adapter.signifyd.models.Shipment;
import com.signifyd.ctconnector.function.adapter.signifyd.models.ShipmentDestination;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reroute.rerouteRequest.RerouteRequestDraft;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class RerouteApiStrategy implements SubscriptionStrategy {

    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    public RerouteApiStrategy(
        CommercetoolsClient commercetoolsClient,
        SignifydClient signifydClient
    ) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
    }

    @Override
    public void execute(Message message) {
        String orderId = message.getResource().getId();
        Order order = this.commercetoolsClient.getOrderById(orderId);
        OrderHelper.controlOrderSentToSignifyd(order);
        var draft = RerouteRequestDraft
                .builder()
                .orderId(order.getOrderNumber() != null ? order.getOrderNumber() : order.getId())
                .shipments(mapShipmentsFromCommercetools(order))
                .build();

        executeWithErrorHandling(draft);
    }

    private void executeWithErrorHandling(RerouteRequestDraft draft) {

        try {
            signifydClient.reroute(draft);
            logger.info("Reroute API Success : Delivery Address Change sent to Signifyd");
        } catch (Signifyd4xxException | Signifyd5xxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Shipment> mapShipmentsFromCommercetools(Order order) {
        var shipment = buildShipment(order.getShippingAddress());
        return Collections.singletonList(shipment);
    }

    private Shipment buildShipment(com.commercetools.api.models.common.Address address) {
        var shipment = Shipment.builder()
                .destination(ShipmentDestination.builder()
                        .fullName(String.format("%s %s", address.getFirstName(),
                                address.getLastName()))
                        .address(Address.builder()
                                .city(address.getCity())
                                .streetAddress(address.getStreetName())
                                .unit(address.getAdditionalStreetInfo())
                                .countryCode(address.getCountry())
                                .provinceCode(address.getCountry())
                                .postalCode(address.getPostalCode())
                                .build())
                        .build())
                .build();
        return shipment;
    }
}
