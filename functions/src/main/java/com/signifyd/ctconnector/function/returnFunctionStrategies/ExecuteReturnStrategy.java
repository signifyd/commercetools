package com.signifyd.ctconnector.function.returnFunctionStrategies;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.commercetools.api.models.type.TypeResourceIdentifierBuilder;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.ReturnInfoTransition;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.executeReturn.ExecuteReturnRequestDraft;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;

import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExecuteReturnStrategy implements ReturnStrategy {

    protected final ConfigReader configReader;
    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public ExecuteReturnStrategy(
        ConfigReader configReader,
        SignifydClient signifydClient,
        SignifydMapper signifydMapper
    ) {
        this.configReader = configReader;
        this.signifydClient = signifydClient;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> execute(Order order, ReturnInfo returnInfo) throws Signifyd4xxException, Signifyd5xxException, IOException {
        ExecuteReturnRequestDraft requestDraft = generateRequest(order, returnInfo);
        signifydClient.executeReturn(requestDraft);
        logger.info("Execute Return API Success: Order successfully sent to Signifyd");
        return generateResponse(order, returnInfo);
    }

    private ExecuteReturnRequestDraft generateRequest(Order order, ReturnInfo returnInfo) {
        return ExecuteReturnRequestDraft
                .builder()
                .orderId(order.getOrderNumber() != null ? order.getOrderNumber() : order.getId())
                .returnId(returnInfo.getReturnTrackingId())
                .build();
    }

    private ExtensionResponse<OrderUpdateAction> generateResponse(
        Order order,
        ReturnInfo returnInfo
    ) throws IOException {
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<OrderUpdateAction>();
        TypeResourceIdentifier type = TypeResourceIdentifierBuilder.of().key(CustomFields.SIGNIFYD_RETURN_ITEM_TYPE).build();
        for (ReturnItem returnItem : returnInfo.getItems()) {
            returnItem.getCustom().getFields().values().put(CustomFields.RETURN_ITEM_TRANSITION, ReturnInfoTransition.RECORD.name());
            response.addAction(
                    OrderSetReturnItemCustomTypeAction.builder()
                            .type(type)
                            .fields(returnItem.getCustom().getFields())
                            .returnItemId(returnItem.getId())
                            .build());
        }
        return response;
    }
}
