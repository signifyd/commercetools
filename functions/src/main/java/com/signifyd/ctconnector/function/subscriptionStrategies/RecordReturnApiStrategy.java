package com.signifyd.ctconnector.function.subscriptionStrategies;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.*;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.recordReturn.RecordReturnRequestDraft;
import com.signifyd.ctconnector.function.utils.OrderHelper;

import java.util.UUID;

import org.slf4j.LoggerFactory;


public class RecordReturnApiStrategy implements SubscriptionStrategy {

    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public RecordReturnApiStrategy(
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

        if (!isEligibleToProcess(order)) {
            logger.debug("Order is not eligible to process for Record Return API.");
            return;
        }
        
        String returnId = UUID.randomUUID().toString();
        RecordReturnRequestDraft draft = RecordReturnRequestDraft
                .builder()
                .orderId(order.getOrderNumber() != null ? order.getOrderNumber() : order.getId())
                .returnId(returnId)
                .build();

        sendRecordReturnRequest(draft);
    }

    private void sendRecordReturnRequest(RecordReturnRequestDraft draft) {
        try {
            this.signifydClient.recordReturn(draft);
            logger.info("Record Return API Success: Order successfully sent to Signifyd");
        } catch (Signifyd4xxException | Signifyd5xxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEligibleToProcess(Order order) {
        OrderState orderState = order.getOrderState();
        ShipmentState shipmentState = order.getShipmentState();

        return orderState == OrderState.CANCELLED
                && (shipmentState == null
                        || (shipmentState != null && shipmentState == ShipmentState.PENDING)
                        || (shipmentState != null && shipmentState == ShipmentState.READY));
    }
}
