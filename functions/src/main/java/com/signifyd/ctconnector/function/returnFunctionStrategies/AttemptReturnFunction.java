package com.signifyd.ctconnector.function.returnFunctionStrategies;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.commercetools.api.models.type.TypeResourceIdentifierBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttemptReturnFunction implements ReturnFunctionStrategy {

    protected final ConfigReader configReader;
    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public AttemptReturnFunction(
            ConfigReader configReader,
            SignifydClient signifydClient,
            SignifydMapper signifydMapper) {
        this.configReader = configReader;
        this.signifydClient = signifydClient;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> execute(Order order, ReturnInfo returnInfo) throws IOException, Signifyd4xxException, Signifyd5xxException {
        if (!isReturnTrackingIdValid(order.getReturnInfo(), returnInfo.getReturnTrackingId())) {
            throw new NullPointerException("Attempt Return API Fail: Return tracking id should be unique and not empty.");
        }

        AttemptReturnRequestDraft requestDraft = generateRequest(order, returnInfo);
        AttemptReturnResponse attemptReturnResponse = signifydClient.attemptReturn(requestDraft);
        logger.info("Attempt Return API Success: Order successfully sent to Signifyd");
        return generateResponse(order, returnInfo, attemptReturnResponse);
    }

    private AttemptReturnRequestDraft generateRequest(Order order, ReturnInfo returnInfo) {
        return AttemptReturnRequestDraft
                .builder()
                .orderId(order.getId())
                .returnId(returnInfo.getReturnTrackingId())
                .returnedItems(signifydMapper.mapReturnedProductsFromCommercetools(order.getLineItems(), returnInfo))
                .device(signifydMapper.mapDeviceFromCommercetools(order))
                .build();
    }

    private ExtensionResponse<OrderUpdateAction> generateResponse(
            Order order,
            ReturnInfo newReturnInfo,
            AttemptReturnResponse attemptReturnResponse) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<OrderUpdateAction>();
        TypeResourceIdentifier type = TypeResourceIdentifierBuilder.of().key(CustomFields.SIGNIFYD_RETURN_ITEM_TYPE)
                .build();

        for (ReturnItem returnItem : newReturnInfo.getItems()) {
            FieldContainer fields = FieldContainer.builder()
                    .addValue(CustomFields.RETURN_ITEM_RAW_DECISION,
                            objMapper.writeValueAsString(attemptReturnResponse.getDecision()))
                    .build();
            response.addAction(
                    OrderSetReturnItemCustomTypeAction.builder()
                            .type(type)
                            .fields(fields)
                            .returnItemId(returnItem.getId())
                            .build());
        }

        List<Decision> prevReturnsDecisions = new ArrayList<>();
        List<Action> prevReturnCheckpointActions = new ArrayList<>();
        for (ReturnInfo returnInfo : order.getReturnInfo().subList(0, order.getReturnInfo().size() - 1)) {
            ReturnItem firstReturnItem = returnInfo.getItems().get(0);
            String rawDecision = firstReturnItem.getCustom().getFields().values()
                    .get(CustomFields.RETURN_ITEM_RAW_DECISION).toString();
            Decision decision = objMapper.readValue(rawDecision, Decision.class);
            Action checkpointAction = decision.getCheckpointAction();
            prevReturnsDecisions.add(decision);
            prevReturnCheckpointActions.add(checkpointAction);
        }
        Decision decision = attemptReturnResponse.getDecision();
        Action checkpointAction = decision.getCheckpointAction();
        prevReturnsDecisions.add(decision);
        prevReturnCheckpointActions.add(checkpointAction);

        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.SIGNIFYD_RETURNS_RAW_DECISION)
                        .value(objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prevReturnsDecisions)).build());
        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.SIGNIFYD_RETURNS_CHECKPOINT_ACTION)
                        .value(objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prevReturnCheckpointActions)).build());

        return response;
    }

    private boolean isReturnTrackingIdValid(List<ReturnInfo> returnInfoList, String returnTrackingId) {
        return returnTrackingId != null
                && returnInfoList.subList(0, returnInfoList.size() - 1).stream()
                        .noneMatch(ri -> ri.getReturnTrackingId().equals(returnTrackingId));
    }
}
