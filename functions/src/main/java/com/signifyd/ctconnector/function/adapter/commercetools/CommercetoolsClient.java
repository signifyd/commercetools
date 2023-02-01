package com.signifyd.ctconnector.function.adapter.commercetools;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.TypeReference;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.config.ConfigReader;
import io.vrap.rmf.base.client.ApiHttpResponse;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CommercetoolsClient {

    private final ProjectApiRoot projectApiRoot;

    private static final int ATTEMPT_COUNT = 3;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public CommercetoolsClient(ConfigReader configReader) {
        this.projectApiRoot = ApiRootBuilder.of()
                .defaultClient(ClientCredentials.of()
                                .withClientId(configReader.getCommercetoolsClientId())
                                .withClientSecret(configReader.getCommercetoolsClientSecret())
                                .build(),
                        this.getServiceRegion(configReader.getCommercetoolsRegion()))
                .build(configReader.getCommerceToolsProjectKey());
    }

    public CommercetoolsClient(ProjectApiRoot projectApiRoot) {
        this.projectApiRoot = projectApiRoot;
    }

    private ServiceRegion getServiceRegion(String region) {
        for (ServiceRegion serviceRegion : ServiceRegion.values()) {
            if (serviceRegion.name().equals(region)) {
                return serviceRegion;
            }
        }
        return ServiceRegion.GCP_EUROPE_WEST1;
    }

    public Customer getCustomerById(String id) {
        try {
            CompletableFuture<ApiHttpResponse<Customer>> response = projectApiRoot.customers()
                    .withId(id)
                    .get()
                    .execute();

            return response.get().getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCustomFieldsByOrderId(String id, FieldContainer fields) {
        Order order = this.getOrderById(id);
        this.setCustomFields(order, fields);
    }

    public Order setCustomFields(Order order, FieldContainer fields) {
        List<OrderUpdateAction> actionList = new ArrayList<>();

        TypeReference type = TypeReference.builder()
                .id(Order.typeReference().getType().getTypeName())
                .build();

        CustomFields customFields = CustomFields.builder()
                .fields(fields)
                .type(type)
                .build();

        for (Entry<String, Object> field : customFields.getFields().values().entrySet()) {
            actionList.add(OrderSetCustomFieldActionBuilder.of()
                    .name(field.getKey())
                    .value(field.getValue())
                    .build());
        }

        return orderUpdate(order, actionList);
    }

    public Order orderUpdate(Order order, List<OrderUpdateAction> actionList) {
        CompletableFuture<ApiHttpResponse<Order>> response;
        int checkAttempt = 0;
        Exception lastError;
        do {
            var orderVersion = order.getVersion();
            if (checkAttempt != 0) {
                Order recentOrder = getOrderById(order.getId());
                orderVersion = recentOrder.getVersion();
            }
            try {

                response = this.projectApiRoot.orders().withId(order.getId()).post(
                                OrderUpdate.builder()
                                        .version(orderVersion)
                                        .actions(actionList)
                                        .build())
                        .execute();
                return response.get().getBody();
            } catch (InterruptedException | ExecutionException e) {
                checkAttempt++;
                lastError = e;
            }

        } while (checkAttempt < ATTEMPT_COUNT);
        logger.error("Order with {} couldn't updated", order.getId());
        throw new RuntimeException(lastError);
    }

    public Order getOrderById(String id) {
        try {
            CompletableFuture<ApiHttpResponse<Order>> response = projectApiRoot.orders()
                    .withId(id)
                    .get()
                    .execute();

            return response.get().getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Order getOrderByPaymentId(String id) {
        try {
            OrderPagedQueryResponse orders = projectApiRoot.orders()
                    .get()
                    .addWhere("paymentInfo(payments(id=:paymentId))")
                    .addPredicateVar("paymentId", id)
                    .withLimit(1)
                    .execute().toCompletableFuture().get()
                    .getBody();

            if (orders.getResults().isEmpty()) {
                throw new RuntimeException(
                        String.format("There is no order found with given payment id: %s", id));
            } else {
                return orders.getResults().get(0);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Payment getPaymentById(String id) {
        if (id == null) {
            return null;
        }
        try {
            CompletableFuture<ApiHttpResponse<Payment>> response = this.projectApiRoot.payments()
                    .withId(id)
                    .get()
                    .execute();

            return response.get().getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Order setCustomType(Order order, String customTypeKey, FieldContainer fields) {
        List<OrderUpdateAction> actionList = new ArrayList<>();

        TypeResourceIdentifier customType = TypeResourceIdentifier.builder()
                .key(customTypeKey)
                .build();

        actionList.add(OrderSetCustomTypeActionBuilder.of()
                    .fields(fields)
                    .type(customType)
                    .build());

        return orderUpdate(order, actionList);
    }
}
