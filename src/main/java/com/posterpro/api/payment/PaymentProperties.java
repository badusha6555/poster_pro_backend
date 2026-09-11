package com.posterpro.api.payment;

import com.posterpro.api.common.PlanTier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {

    private String activeProvider = "razorpay";
    private String currency = "INR";
    private Razorpay razorpay = new Razorpay();
    private Plans plans = new Plans();

    /**
     * Server-side price list, keyed by plan tier. The amount charged for a
     * subscription MUST always be looked up here — never accepted from the
     * client — otherwise a modified app could request a PREMIUM subscription
     * while paying the price of a cheaper (or ₹0) tier.
     */
    public BigDecimal amountFor(PlanTier tier) {
        return switch (tier) {
            case FREE -> BigDecimal.ZERO;
            case BASIC -> plans.getBasic();
            case PREMIUM -> plans.getPremium();
            case ENTERPRISE -> plans.getEnterprise();
        };
    }

    @Getter
    @Setter
    public static class Razorpay {
        private String keyId;
        private String keySecret;
        /** Separate secret configured in the Razorpay dashboard for webhook payloads — not the API key secret. */
        private String webhookSecret;
    }

    /**
     * PLACEHOLDER prices (monthly, INR) — confirm/replace with real pricing
     * before going live. Overridable per-environment via PLAN_PRICE_* env vars.
     */
    @Getter
    @Setter
    public static class Plans {
        private BigDecimal basic = new BigDecimal("299");
        private BigDecimal premium = new BigDecimal("599");
        private BigDecimal enterprise = new BigDecimal("1499");
    }
}
