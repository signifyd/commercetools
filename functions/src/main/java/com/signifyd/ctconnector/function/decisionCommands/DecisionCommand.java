package com.signifyd.ctconnector.function.decisionCommands;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderSetCustomFieldActionBuilder;
import com.commercetools.api.models.order.OrderState;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;

import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ActionType;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfig;
import com.signifyd.ctconnector.function.constants.CustomFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.signifyd.ctconnector.function.constants.CustomFields.*;
import static com.signifyd.ctconnector.function.constants.SignifydApi.*;

public abstract class DecisionCommand {

    protected final DecisionResponse decisionResponse;
    protected final CommercetoolsClient commercetoolsClient;
    protected final ConfigReader configReader;
    protected final Order order;
    private final Action action;


    public DecisionCommand(ConfigReader configReader,
                           CommercetoolsClient commercetoolsClient,
                           Order order,
                           DecisionResponse decisionResponse,
                           Action action) {
        this.configReader = configReader;
        this.commercetoolsClient = commercetoolsClient;
        this.order = order;
        this.decisionResponse = decisionResponse;
        this.action = action;
    }

    public List<OrderUpdateAction> prepareStandardOrderActions() {
        List<OrderUpdateAction> actionList = new ArrayList<>();
        Map<String, Object> customFieldUpdates = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String signifydOrderUrl = SIGNIFYD_ORDER_URL + this.decisionResponse.getSignifydId();
        String decisionRaw = "";
        try {
            decisionRaw = objectMapper.writeValueAsString(this.decisionResponse.getDecision());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        customFieldUpdates.put(CHECKOUT_ID, this.order.getCustom().getFields().values().get(CustomFields.CHECKOUT_ID).toString());
        customFieldUpdates.put(FRAUD_CHECK_POINT_ACTION, this.action.toString());
        customFieldUpdates.put(FRAUD_RAW_DECISION, decisionRaw);
        customFieldUpdates.put(FRAUD_CHECKPOINT_ACTION_REASON, this.decisionResponse.getDecision().checkpointActionReason);
        customFieldUpdates.put(FRAUD_SCORE, this.decisionResponse.getDecision().score);
        customFieldUpdates.put(SIGNIFYD_ID, this.decisionResponse.getSignifydId());
        customFieldUpdates.put(ORDER_URL_FIELD, signifydOrderUrl);
        if (this.decisionResponse.getScaEvaluation() != null) {
            customFieldUpdates.put(SCA_OUTCOME, this.decisionResponse.getScaEvaluation().getOutcome());
            if (this.decisionResponse.getScaEvaluation().getExemptionDetails() != null) {
                customFieldUpdates.put(SCA_EXEMPTION, this.decisionResponse.getScaEvaluation().getExemptionDetails().getExemption());
                customFieldUpdates.put(SCA_EXEMPTION_PLACEMENT, this.decisionResponse.getScaEvaluation().getExemptionDetails().getPlacement());
            }
            if (this.decisionResponse.getScaEvaluation().getExclusionDetails() != null) {
                customFieldUpdates.put(SCA_EXCLUSION, this.decisionResponse.getScaEvaluation().getExclusionDetails().getExclusion());
            }
        }

        for (Map.Entry<String, Object> field : customFieldUpdates.entrySet()) {
            actionList.add(OrderSetCustomFieldActionBuilder.of()
                    .name(field.getKey())
                    .value(field.getValue())
                    .build());
        }
        return actionList;
    }

    protected List<OrderUpdateAction> generateDefaultOrderActions(DecisionActionConfig decisionConfig) {
        List<OrderUpdateAction> actionList = new ArrayList<>(prepareStandardOrderActions());
        if (ActionType.NONE.equals(decisionConfig.getActionType())) {
            return actionList;
        } else if (ActionType.DEFAULT_STATE_TRANSITION.equals(decisionConfig.getActionType())) {
            actionList.add(
                    OrderUpdateAction
                            .changeOrderStateBuilder()
                            .orderState(OrderState
                                    .findEnum(decisionConfig.getDefaultState()))
                            .build());
        } else if (ActionType.CUSTOM_STATE_TRANSITION.equals(decisionConfig.getActionType())) {
            actionList.add(
                    OrderUpdateAction
                            .transitionStateBuilder()
                            .state(builder -> builder.key(decisionConfig.getCustomStateKey()))
                            .force(decisionConfig.isForceTransition())
                            .build());
        } else {
            throw new IllegalArgumentException(
                    String.format("Decision making for %s is not handled", ActionType.DO_NOT_CREATE_ORDER));
        }
        return actionList;
    }

    public Order executeStandardOrderActions() {
        List<OrderUpdateAction> actions = new ArrayList<>(prepareStandardOrderActions());
        return commercetoolsClient.orderUpdate(this.order, actions);
    }

    protected Order executeDefaultOrderActions(DecisionActionConfig decisionConfig) {
        List<OrderUpdateAction> actions = new ArrayList<>(generateDefaultOrderActions(decisionConfig));
        return commercetoolsClient.orderUpdate(this.order, actions);
    }

    public abstract List<OrderUpdateAction> generateOrderActions();

    public abstract Order executeOrderActions();
}
