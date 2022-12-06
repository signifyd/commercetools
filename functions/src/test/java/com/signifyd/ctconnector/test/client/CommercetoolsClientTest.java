package com.signifyd.ctconnector.test.client;


import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.customer.CustomerImpl;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import io.vrap.rmf.base.client.ApiHttpHeaders;
import io.vrap.rmf.base.client.ApiHttpResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommercetoolsClientTest {

    private CommercetoolsClient commercetoolsClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProjectApiRoot projectApiRoot;

    @BeforeEach
    public void setup() {
        commercetoolsClient = new CommercetoolsClient(projectApiRoot);
    }

    @SneakyThrows
    @Test
    public void getCustomerById() {
        // given
        String customerId = "customerId";
        Customer customer = new CustomerImpl();
        CompletableFuture<ApiHttpResponse<Customer>> response = CompletableFuture.completedFuture(new ApiHttpResponse<Customer>(
                200,
                new ApiHttpHeaders(),
                customer));
        // when
        when(projectApiRoot.customers().withId(customerId).get().execute()).thenReturn(response);
        // then
        Customer customerResponse = commercetoolsClient.getCustomerById(customerId);
        assertThat(customerResponse).isEqualTo(customer);
    }
}
