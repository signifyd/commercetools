package com.signifyd.ctconnector.function.subscriptionStrategies;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperFactory;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperType;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionRequest.TransactionRequestDraft;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransactionApiStrategy implements SubscriptionStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    public TransactionApiStrategy(
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
        String paymentId = message.getResource().getId();
        Order order = this.commercetoolsClient.getOrderByPaymentId(paymentId);
        if (!configReader.isPreAuth(order.getCountry())) {
            return;
        }
        OrderHelper.controlOrderSentToSignifyd(order);
        var draft = TransactionRequestDraft
                .builder()
                .orderId(order.getId())
                .checkoutId(order.getCustom().getFields().values().get(CustomFields.CHECKOUT_ID).toString())
                .transactions(getTransactionsFromPayment(paymentId, order))
                .build();
        try {
            signifydClient.transaction(draft);
            logger.info("Transaction API Success: Transactions Update sent to Signifyd");
        } catch (Signifyd4xxException | Signifyd5xxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TransactionModel> getTransactionsFromPayment(
            String paymentId,
            Order order) {
        Payment payment = this.commercetoolsClient.getPaymentById(paymentId);
        TransactionMapperFactory transactionMapperFactory = new TransactionMapperFactory(order, payment, configReader);
        return transactionMapperFactory.generateTransactionMapper(TransactionMapperType.POST_AUTH).generateTransactions();
    }
}
