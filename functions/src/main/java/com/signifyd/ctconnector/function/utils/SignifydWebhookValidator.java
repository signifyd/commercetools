package com.signifyd.ctconnector.function.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.signifyd.ctconnector.function.adapter.signifyd.enums.Checkpoint;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;

public class SignifydWebhookValidator {
    public static boolean validateWebhook(String signifydSecHmacSha256, String requestBody, String SignifydTeamAPIKey) {
        try {
            String encodedBody = encryptBody(requestBody, SignifydTeamAPIKey);
            byte[] bytesOfEncodedBody = encodedBody.getBytes(StandardCharsets.UTF_8);
            byte[] bytesOfSIGNIFYD_SEC_HMAC_SHA256 = signifydSecHmacSha256.getBytes(StandardCharsets.UTF_8);
            return java.security.MessageDigest.isEqual(bytesOfSIGNIFYD_SEC_HMAC_SHA256, bytesOfEncodedBody);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encryptBody(String body, String SignifydTeamAPIKey)
            throws NoSuchAlgorithmException, InvalidKeyException,
            UnsupportedEncodingException {
        Mac sha256HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(SignifydTeamAPIKey.getBytes(), "HmacSHA256");
        sha256HMAC.init(secretKey);
        return Base64.getEncoder().encodeToString(sha256HMAC.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    public static Boolean isExcessiveWebHook(WebhookRequest webhookRequest) {
        return webhookRequest.getSignifydCheckpoint().equals(Checkpoint.CHECKOUT.name()) ||
            webhookRequest.getSignifydCheckpoint().equals(Checkpoint.TRANSACTION.name());
    }
}
