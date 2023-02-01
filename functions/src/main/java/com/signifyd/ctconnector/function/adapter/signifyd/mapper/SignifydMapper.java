package com.signifyd.ctconnector.function.adapter.signifyd.mapper;

import com.commercetools.api.models.cart.LineItem;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.order.LineItemReturnItem;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.ReturnInfo;
import com.commercetools.api.models.order.ReturnItem;
import com.signifyd.ctconnector.function.adapter.signifyd.models.UserAccount;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.ReturnItemReason;
import com.signifyd.ctconnector.function.adapter.signifyd.models.*;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.phoneNumber.PhoneNumberField;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import com.signifyd.ctconnector.function.utils.Price;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignifydMapper {

    public Purchase mapPurchaseFromCommercetools(Customer customer, Order order, PhoneNumberField phoneNumberField) {
        List<Product> productList = this.mapProductsFromCommercetools(order.getLineItems());
        return Purchase
                .builder()
                .createdAt(order.getCreatedAt().toOffsetDateTime().toString())
                .orderChannel(order.getCustom().getFields().values().get(CustomFields.ORDER_CHANNEL).toString())
                .totalPrice(Price.commerceToolsPrice(order.getTotalPrice()))
                .totalShippingCost(Price.commerceToolsPrice(order.getShippingInfo().getPrice()))
                .currency(order.getTotalPrice().getCurrencyCode())
                .products(productList)
                .confirmationPhone(OrderHelper.getPhoneNumberFromOrder(customer, order, phoneNumberField))
                .confirmationEmail(OrderHelper.getConfirmationEmail(customer, order))
                .shipments(mapShipmentsFromCommercetools(order))
                .build();
    }

    public List<Shipment> mapShipmentsFromCommercetools(Order order) {
        var shipment = Shipment.builder()
                .destination(ShipmentDestination.builder()
                        .fullName(String.format("%s %s",
                                order.getShippingAddress().getFirstName(),
                                order.getShippingAddress().getLastName()))
                        .address(Address.builder()
                                .city(order.getShippingAddress().getCity())
                                .streetAddress(order.getShippingAddress()
                                        .getStreetName())
                                .unit(order.getShippingAddress().getAdditionalStreetInfo())
                                .countryCode(order.getShippingAddress().getCountry())
                                .provinceCode(order.getShippingAddress().getCountry())
                                .postalCode(order.getShippingAddress().getPostalCode())
                                .build())
                        .build())
                .build();
        return Collections.singletonList(shipment);
    }

    public List<Product> mapProductsFromCommercetools(List<LineItem> lineItems) {
        ConfigReader configReader = new ConfigReader();
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

    public List<ReturnedProduct> mapReturnedProductsFromCommercetools(List<LineItem> lineItems, ReturnInfo returnInfo)
            throws NullPointerException {
        ConfigReader configReader = new ConfigReader();
        List<ReturnedProduct> productList = new ArrayList<>();

        for (ReturnItem returnItem : returnInfo.getItems()) {
            LineItemReturnItem lineItemReturnItem = (LineItemReturnItem) returnItem;
            LineItem item = lineItems.stream().filter(li -> li.getId().equals(lineItemReturnItem.getLineItemId()))
                    .findFirst()
                    .orElseThrow();

            String reason;
            try {
                reason = ReturnItemReason.valueOf(lineItemReturnItem.getComment()).name();
            } catch (NullPointerException | IllegalArgumentException e) {
                throw new NullPointerException(
                        String.format("Return item \"%s\" has not a valid reason.",
                                item.getName().get(configReader.getLocale())));
            }

            productList.add(ReturnedProduct
                    .builder()
                    .reason(reason)
                    .itemName(item.getName().get(configReader.getLocale()))
                    .itemPrice(Price.commerceToolsPrice(item.getTotalPrice()))
                    .itemQuantity(item.getQuantity().intValue())
                    .itemIsDigital(Boolean.FALSE)
                    .itemId(item.getId())
                    .build());
        }

        return productList;
    }

    public Device mapDeviceFromCommercetools(Order order) {
        if (order.getCustom() != null
                && order.getCustom().getFields().values().get(CustomFields.SESSION_ID) != null
                && order.getCustom().getFields().values().get(CustomFields.CLIENT_IP_ADDRESS) != null
        ) {
            return Device.builder()
                    .clientIpAddress(order.getCustom().getFields().values().get(CustomFields.CLIENT_IP_ADDRESS)
                            .toString())
                    .sessionId(order.getCustom().getFields().values().get(CustomFields.SESSION_ID).toString())
                    .build();
        }
        return null;
    }

    public UserAccount mapUserAccountFromCommercetools(Customer customer, PhoneNumberField phoneNumberField) {
        if (customer == null)
            return null;
        return UserAccount.builder()
                .username(customer.getEmail())
                .createdDate(customer.getCreatedAt().toOffsetDateTime().toString())
                .email(customer.getEmail())
                .phone(OrderHelper.getCustomerPhoneNumber(customer, phoneNumberField))
                .build();
    }
}
