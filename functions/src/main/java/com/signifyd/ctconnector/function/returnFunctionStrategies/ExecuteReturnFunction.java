package com.signifyd.ctconnector.function.returnFunctionStrategies;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.order.*;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class ExecuteReturnFunction implements ReturnFunctionStrategy {

    protected final ConfigReader configReader;
    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public ExecuteReturnFunction(
            ConfigReader configReader,
            SignifydClient signifydClient,
            SignifydMapper signifydMapper) {
        this.configReader = configReader;
        this.signifydClient = signifydClient;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> execute(Order order, ReturnInfo returnInfo) throws Signifyd4xxException, Signifyd5xxException, IOException {
        ExecuteReturnRequestDraft requestDraft = generateRequest(order, returnInfo);
        ExecuteReturnResponse executeReturnResponse = signifydClient.executeReturn(requestDraft);
        logger.info("Execute Return API Success: Order successfully sent to Signifyd");
        return generateResponse(order, returnInfo, executeReturnResponse);
    }

    private ExecuteReturnRequestDraft generateRequest(Order order, ReturnInfo returnInfo) {
        return ExecuteReturnRequestDraft
                .builder()
                .orderId(order.getId())
                .returnId(returnInfo.getReturnTrackingId())
                .build();
    }

    private ExtensionResponse<OrderUpdateAction> generateResponse(
            Order order,
            ReturnInfo newReturnInfo,
            ExecuteReturnResponse executeReturnResponse) throws IOException {
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<OrderUpdateAction>();
        response.setActions(new ArrayList<>());
        return response;
    }
}
