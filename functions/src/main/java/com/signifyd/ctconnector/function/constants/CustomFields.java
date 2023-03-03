package com.signifyd.ctconnector.function.constants;

public final class CustomFields {

    public CustomFields() {
        throw new IllegalStateException();
    }

    // Order Custom Fields
    public final static String CHECKOUT_ID = "checkoutId";
    public final static String SESSION_ID = "sessionId";
    public final static String SIGNIFYD_ERROR = "signifydError";
    public final static String FRAUD_RAW_DECISION = "fraudRawDecision";
    public final static String SIGNIFYD_ID = "signifydId";
    public final static String FRAUD_CHECK_POINT_ACTION = "fraudCheckpointAction";
    public final static String FRAUD_CHECKPOINT_ACTION_REASON = "fraudCheckpointActionReason";
    public final static String FRAUD_SCORE = "fraudScore";
    public final static String CURRENT_PRICE = "currentPrice";
    public final static String IS_SENT_TO_SIGNIFYD = "isSentToSignifyd";
    public final static String SCA_OUTCOME = "scaOutcome";
    public final static String SCA_EXEMPTION = "scaExemption";
    public final static String SCA_EXEMPTION_PLACEMENT = "scaExemptionPlacement";
    public final static String SCA_EXCLUSION = "scaExclusion";
    public final static String ORDER_CHANNEL = "orderChannel";
    public final static String CLIENT_IP_ADDRESS = "clientIpAddress";
    public final static String ORDER_URL_FIELD = "signifydOrderUrl";
    public final static String SIGNIFYD_RETURNS_RAW_DECISION = "signifydReturnsRawDecision";
    public final static String SIGNIFYD_RETURNS_CHECKPOINT_ACTION = "signifydReturnsCheckpointAction";

    // Payment Custom Fields
    public final static String CARD_HOLDER_NAME = "cardHolderName";
    public final static String CARD_BIN = "cardBin";
    public final static String CARD_LAST_FOUR = "cardLastFour";
    public final static String CARD_EXPIRY_MONTH = "cardExpiryMonth";
    public final static String CARD_EXPIRY_YEAR = "cardExpiryYear";

    // Order Return Item Custom Fields
    public final static String RETURN_ITEM_RAW_ATTEMPT_RESPONSE = "rawAttemptResponse";
    public final static String RETURN_ITEM_TRANSITION = "transition";

    // API Extension
    public final static String INVALID_INPUT = "InvalidInput";
    public final static String FAILED_VALIDATION = "FailedValidation";

    // Order Custom Type
    public final static String SIGNIFYD_ORDER_TYPE_KEY = "signifyd-order-type";
    public final static String SIGNIFYD_RETURN_ITEM_TYPE = "signifyd-return-item-type";
}
