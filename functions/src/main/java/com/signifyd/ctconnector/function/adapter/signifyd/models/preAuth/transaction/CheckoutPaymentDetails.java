package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.signifyd.ctconnector.function.adapter.signifyd.models.BillingAddress;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft.CardInstallments;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class CheckoutPaymentDetails implements Serializable {
    @JsonProperty("billingAddress")
    private BillingAddress billingAddress;
    @JsonProperty("accountHolderName")
    private String accountHolderName;
    @JsonProperty("accountHolderTaxId")
    private String accountHolderTaxId;
    @JsonProperty("accountHolderTaxIdCountry")
    private String accountHolderTaxIdCountry;
    @JsonProperty("accountLast4")
    private String accountLast4;
    @JsonProperty("abaRoutingNumber")
    private String abaRoutingNumber;
    @JsonProperty("cardToken")
    private String cardToken;
    @JsonProperty("cardTokenProvider")
    private String cardTokenProvider;
    @JsonProperty("cardBin")
    private String cardBin; // ^(\d{6}|\d{8})$
    @JsonProperty("cardExpiryMonth")
    private Integer cardExpiryMonth;
    @JsonProperty("cardExpiryYear")
    private Integer cardExpiryYear;
    @JsonProperty("cardLast4")
    private String cardLast4;
    @JsonProperty("cardBrand")
    private String cardBrand;
    @JsonProperty("cardFunding")
    private String cardFunding;
    @JsonProperty("cardInstallments")
    private CardInstallments cardInstallments;
}
