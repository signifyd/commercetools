package com.signifyd.ctconnector.function.utils;

import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.order.Order;
import com.signifyd.ctconnector.function.config.model.phoneNumber.PhoneNumberField;
import com.signifyd.ctconnector.function.constants.CustomFields;

public class OrderHelper {

    private static boolean isOrderSentToSignifyd(Order order) {
        return order.getCustom() != null &&
                order.getCustom().getFields().values()
                        .getOrDefault(CustomFields.IS_SENT_TO_SIGNIFYD, false)
                        .toString()
                        .equals("true");
    }

    public static void controlOrderSentToSignifyd(Order order) throws RuntimeException {
        if (!isOrderSentToSignifyd(order)) {
            throw new RuntimeException("Order was not sent to Signifyd. The process cannot go on.");
        }
    }

    public static String getPhoneNumberFromOrder(Customer customer, Order order, PhoneNumberField phoneNumberField) {
        String shippingAddressPhoneNumber = order.getShippingAddress().getPhone();
        String billingAddressPhoneNumber = order.getBillingAddress().getPhone();
        String customerPhoneNumber = getCustomerPhoneNumber(customer, phoneNumberField);
        return (billingAddressPhoneNumber != null && !billingAddressPhoneNumber.equals("")) ? billingAddressPhoneNumber
                : ((shippingAddressPhoneNumber != null && !shippingAddressPhoneNumber.equals(""))
                        ? shippingAddressPhoneNumber
                        : customerPhoneNumber);
    }

    public static String getConfirmationEmail(Customer customer, Order order) {
        if (order.getBillingAddress().getEmail() != null) {
            return order.getBillingAddress().getEmail();
        } else if (order.getShippingAddress().getEmail() != null) {
            return order.getShippingAddress().getEmail();
        } else if (customer != null) {
            return customer.getEmail();
        }
        return order.getCustomerEmail();
    }

    public static String getCustomerPhoneNumber(Customer customer, PhoneNumberField phoneNumberField) {
        if (customer != null && customer.getCustom() != null && phoneNumberField != null) {
            Object customerPhoneNumber = customer.getCustom().getFields().values()
                    .get(phoneNumberField.getCustomerPhoneNumberField());
            return customerPhoneNumber != null ? customerPhoneNumber.toString() : null;
        }
        return null;
    }

    public static String getMostRecentPaymentIdFromOrder(Order order) {
        if (order.getPaymentInfo() != null && !order.getPaymentInfo().getPayments().isEmpty()) {
            var count = order.getPaymentInfo().getPayments().size();
            return order.getPaymentInfo().getPayments().get(count - 1).getId();
        }
        return null;
    }
}