package com.signifyd.ctconnector.function.returnFunctionStrategies;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.order.*;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.commercetools.api.models.type.TypeResourceIdentifierBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.ReturnInfoTransition;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnRequestDraft;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.AttemptReturnResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn.CheckpointAction;
import com.signifyd.ctconnector.function.constants.CustomFields;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttemptReturnStrategy implements ReturnStrategy {

    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public AttemptReturnStrategy(
        SignifydClient signifydClient,
        SignifydMapper signifydMapper
    ) {
        this.signifydClient = signifydClient;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> execute(Order order, ReturnInfo returnInfo) throws IOException, Signifyd4xxException, Signifyd5xxException {
        if (!isReturnTrackingIdValid(order.getReturnInfo(), returnInfo.getReturnTrackingId())) {
            throw new NullPointerException("Return tracking id should be unique and not empty.");
        }

        AttemptReturnRequestDraft requestDraft = generateRequest(order, returnInfo);
        AttemptReturnResponse attemptReturnResponse = signifydClient.attemptReturn(requestDraft);
        logger.info("Attempt Return API Success: Order successfully sent to Signifyd");
        return generateResponse(order, returnInfo, attemptReturnResponse);
    }

    private AttemptReturnRequestDraft generateRequest(Order order, ReturnInfo returnInfo) {
        return AttemptReturnRequestDraft
                .builder()
                .orderId(order.getOrderNumber() != null ? order.getOrderNumber() : order.getId())
                .returnId(returnInfo.getReturnTrackingId())
                .returnedItems(signifydMapper.mapReturnedProductsFromCommercetools(order.getLineItems(), returnInfo))
                .device(signifydMapper.mapDeviceFromCommercetools(order))
                .build();
    }

    private ExtensionResponse<OrderUpdateAction> generateResponse(
        Order order,
        ReturnInfo returnInfo,
        AttemptReturnResponse attemptReturnResponse
    ) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<OrderUpdateAction>();
        TypeResourceIdentifier type = TypeResourceIdentifierBuilder.of().key(CustomFields.SIGNIFYD_RETURN_ITEM_TYPE)
                .build();

        for (ReturnItem returnItem : returnInfo.getItems()) {
            FieldContainer fields = FieldContainer.builder()
                    .addValue(CustomFields.RETURN_ITEM_RAW_ATTEMPT_RESPONSE,
                            objMapper.writeValueAsString(attemptReturnResponse))
                    .addValue(CustomFields.RETURN_ITEM_TRANSITION, ReturnInfoTransition.ATTEMPT.name())
                    .build();
            response.addAction(
                    OrderSetReturnItemCustomTypeAction.builder()
                            .type(type)
                            .fields(fields)
                            .returnItemId(returnItem.getId())
                            .build());
        }

        List<AttemptReturnResponse> prevAttemptResponses = new ArrayList<>();
        List<CheckpointAction> prevCheckpointActions = new ArrayList<>();
        for (ReturnInfo subReturnInfo : order.getReturnInfo().subList(0, order.getReturnInfo().size() - 1)) {
            String rawAttemptResponse = subReturnInfo.getItems().get(0).getCustom().getFields().values()
                    .get(CustomFields.RETURN_ITEM_RAW_ATTEMPT_RESPONSE).toString();
            AttemptReturnResponse attemptResponse = objMapper.readValue(rawAttemptResponse, AttemptReturnResponse.class);
            prevAttemptResponses.add(attemptResponse);
            Action action = attemptResponse.getDecision().getCheckpointAction();
            CheckpointAction checkpointAction = CheckpointAction.builder().returnId(subReturnInfo.getReturnTrackingId()).checkpointAction(action).build();
            prevCheckpointActions.add(checkpointAction);
        }
        prevAttemptResponses.add(attemptReturnResponse);
        Action action = attemptReturnResponse.getDecision().getCheckpointAction();
        CheckpointAction checkpointAction = CheckpointAction.builder().returnId(returnInfo.getReturnTrackingId()).checkpointAction(action).build();
        prevCheckpointActions.add(checkpointAction);

        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.SIGNIFYD_RETURNS_RAW_DECISION)
                        .value(objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prevAttemptResponses)).build());
        response.addAction(
                OrderSetCustomFieldActionBuilder.of()
                        .name(CustomFields.SIGNIFYD_RETURNS_CHECKPOINT_ACTION)
                        .value(objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(prevCheckpointActions)).build());

        return response;
    }

    private boolean isReturnTrackingIdValid(List<ReturnInfo> returnInfoList, String returnTrackingId) {
        return returnTrackingId != null
                && returnInfoList.subList(0, returnInfoList.size() - 1).stream()
                        .noneMatch(ri -> ri.getReturnTrackingId() == null || ri.getReturnTrackingId().equals(returnTrackingId));
    }
}
