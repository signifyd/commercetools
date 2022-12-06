package com.signifyd.ctconnector.test.functions;

import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscriptionsTest {

    @Mock
    private ConfigReader configReader;

    @Mock
    private CommercetoolsClient commercetoolsClient;

    @InjectMocks
    private SubscriptionFunction subscriptionFunction;

}
