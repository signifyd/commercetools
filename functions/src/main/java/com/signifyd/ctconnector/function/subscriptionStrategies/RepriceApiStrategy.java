package com.signifyd.ctconnector.function.subscriptionStrategies;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.cart.LineItem;
import com.commercetools.api.models.common.Money;
import com.commercetools.api.models.common.TypedMoney;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.FieldContainer;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.models.*;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice.PurchaseReprice;
import com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.reprice.RepriceRequestDraft;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import com.signifyd.ctconnector.function.utils.Price;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RepriceApiStrategy implements SubscriptionStrategy {

    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public RepriceApiStrategy(
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
        String orderId = message.getResource().getId();
        Order order = this.commercetoolsClient.getOrderById(orderId);
        OrderHelper.controlOrderSentToSignifyd(order);

        if (!isEligibleToProcess(order)) {
            logger.debug("No reprice action detected");
            return;
        }

        RepriceRequestDraft draft = RepriceRequestDraft
                .builder()
                .orderId(order.getId())
                .purchase(mapPurchseFromCommercetools(order))
                .build();
        sendRepriceRequest(draft);
        setCurrentPriceCustomField(order);
    }

    private void sendRepriceRequest(RepriceRequestDraft draft) {
        try {
            this.signifydClient.reprice(draft);
            logger.info("Reprice API Success: Price changes successfully sent to Signifyd for order with {} id", draft.getOrderId());
        } catch (Signifyd4xxException | Signifyd5xxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Product> mapProductsFromCommercetools(List<LineItem> lineItems) {
        List<Product> productList = new ArrayList<>();
        for (LineItem item : lineItems) {
            productList.add(Product
                    .builder()
                    .itemName(item.getName().get(configReader.getLocale()))
                    .itemPrice(Price.commerceToolsPrice(item.getTotalPrice()))
                    .itemQuantity(item.getQuantity().intValue())
                    .itemIsDigital(Boolean.FALSE)
                    .itemId(item.getId())
                    .build());
        }
        return productList;
    }

    private PurchaseReprice mapPurchseFromCommercetools(Order order) {
        List<Product> productList = this.mapProductsFromCommercetools(order.getLineItems());
        return PurchaseReprice
                .builder()
                .totalPrice(Price.commerceToolsPrice(order.getTotalPrice()))
                .currency(order.getTotalPrice().getCurrencyCode())
                .products(productList)
                .totalShippingCost(Price.commerceToolsPrice(order.getShippingInfo().getPrice()))
                .build();
    }

    private boolean isEligibleToProcess(Order order) {
        TypedMoney totalPrice = order.getTotalPrice();
        TypedMoney currentPrice = (TypedMoney) order.getCustom().getFields().values().get(CustomFields.CURRENT_PRICE);
        boolean isCurrenciesAreEqual = currentPrice.getCurrencyCode().equals(totalPrice.getCurrencyCode());
        boolean isPricesAreEqual = currentPrice.getCentAmount().equals(totalPrice.getCentAmount());
        return isCurrenciesAreEqual && !isPricesAreEqual;
    }

    private Order setCurrentPriceCustomField(Order order) {
        TypedMoney totalPrice = order.getTotalPrice();
        FieldContainer fields = FieldContainer.builder()
                .addValue(CustomFields.CURRENT_PRICE,
                        Money.builder()
                                .centAmount(totalPrice.getCentAmount())
                                .currencyCode(totalPrice.getCurrencyCode())
                                .build())
                .build();

        return this.commercetoolsClient.setCustomFields(order, fields);
    }
}
