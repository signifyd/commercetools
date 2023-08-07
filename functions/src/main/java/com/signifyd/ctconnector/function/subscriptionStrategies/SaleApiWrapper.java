package com.signifyd.ctconnector.function.subscriptionStrategies;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.common.Money;
import com.commercetools.api.models.common.TypedMoney;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.order.*;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.TransactionState;
import com.commercetools.api.models.payment.TransactionType;
import com.commercetools.api.models.type.FieldContainer;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.CoverageRequests;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperFactory;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.TransactionMapperType;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.MerchantPlatform;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.SignifydClientInfo;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postAuth.sale.SaleRequestDraft;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SaleApiWrapper {

    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;
    private final PropertyReader propertyReader;
    private final SignifydMapper signifydMapper;
    private final Order order;
    private final Payment payment;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public SaleApiWrapper(
        CommercetoolsClient commercetoolsClient,
        SignifydClient signifydClient,
        ConfigReader configReader,
        Order order,
        Payment payment
    ) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.configReader = configReader;
        this.order = order;
        this.payment = payment;
        this.propertyReader = new PropertyReader();
        this.signifydMapper = new SignifydMapper();
    }

    public void execute() {
        if (configReader.isPreAuth(order.getCountry())) {
            return;
        }
        if (payment != null && !isOrderEligibleToProcess(order, payment)) {
            setFailOrderCustomFields(order);
            return;
        }
        if (payment != null
                && !payment.getTransactions().isEmpty()
                && !isOrderReadyForSaleApiCall(order, payment)
        ) {
            return;
        }
        Customer customer = order.getCustomerId() != null
                ? this.commercetoolsClient.getCustomerById(order.getCustomerId())
                : null;
        sendSaleRequest(order, payment, customer);
    }

    private void sendSaleRequest(Order order, Payment payment, Customer customer) {
        try {
            signifydClient.sales(generateRequest(order, payment, customer));
            logger.info("Sale API Success: Order successfully sent to Signifyd");
            setSuccessOrderCustomFields(order);

        } catch (Signifyd4xxException | Signifyd5xxException e) {
            setFailOrderCustomFields(order);
        }
    }

    private SaleRequestDraft generateRequest(Order order, Payment payment, Customer customer) {

        TransactionMapperFactory transactionMapperFactory = new TransactionMapperFactory(order, payment, configReader);
        var builder = SaleRequestDraft.builder()
                .orderId(order.getOrderNumber() != null ? order.getOrderNumber() : order.getId())
                .transactions(transactionMapperFactory.generateTransactionMapper(TransactionMapperType.POST_AUTH)
                        .generateTransactions())
                .purchase(signifydMapper.mapPurchaseFromCommercetools(customer, order,
                        configReader.getPhoneNumberFieldMapping()))
                .device(signifydMapper.mapDeviceFromCommercetools(order))
                .userAccount(signifydMapper.mapUserAccountFromCommercetools(customer,
                        configReader.getPhoneNumberFieldMapping()))
                .merchantPlatform(MerchantPlatform.builder().name(SignifydApi.MERCHANT_PLATFORM)
                        .version(this.propertyReader.getCommercetoolsSDKVersion()).build())
                .signifydClient(SignifydClientInfo.builder().application(SignifydApi.SIGNIFYD_CLIENT_INFO)
                        .version(this.propertyReader.getSignifydClientVersion()).build())
                .coverageRequests(Collections.singletonList(CoverageRequests.FRAUD.name()));

        if (configReader.isRecommendationOnly(order.getCountry())) {
            builder.coverageRequests(Collections.singletonList(CoverageRequests.NONE.name()));
        }
        if (ExecutionMode.PASSIVE.equals(configReader.getExecutionMode())) {
            builder.tags(Collections.singletonList(SignifydApi.PASSIVE_MODE));
        }

        return builder.build();
    }

    private boolean isOrderEligibleToProcess(Order order, Payment payment) {
        if (order.getCustom().getFields().values().containsKey(CustomFields.IS_SENT_TO_SIGNIFYD)) {
            return false;
        }
        if (this.configReader.getExcludedPaymentMethods().stream()
                .anyMatch(p -> p.equals(payment.getPaymentMethodInfo().getMethod()))) {
            return false;
        }
        return true;
    }

    private boolean isOrderReadyForSaleApiCall(Order order, Payment payment) {
        boolean hasAuthorizationSuccessTransactions = payment.getTransactions().stream()
                .filter(t -> t.getType().equals(TransactionType.AUTHORIZATION))
                .anyMatch(t -> t.getState().equals(TransactionState.SUCCESS));

        boolean isOrderPaymentStatusIsPaid = PaymentState.PAID.equals(order.getPaymentState());

        return hasAuthorizationSuccessTransactions || isOrderPaymentStatusIsPaid;
    }

    private Order setSuccessOrderCustomFields(Order order) {
        TypedMoney totalPrice = order.getTotalPrice();
        FieldContainer fields = FieldContainer.builder()
                .addValue(CustomFields.CURRENT_PRICE,
                        Money.builder().centAmount(totalPrice.getCentAmount())
                                .currencyCode(totalPrice.getCurrencyCode()).build())
                .addValue(CustomFields.IS_SENT_TO_SIGNIFYD, true).build();

        return this.commercetoolsClient.setCustomFields(order, fields);
    }

    private Order setFailOrderCustomFields(Order order) {
        FieldContainer fields = FieldContainer.builder().addValue(CustomFields.IS_SENT_TO_SIGNIFYD, false).build();

        return this.commercetoolsClient.setCustomFields(order, fields);
    }
}
