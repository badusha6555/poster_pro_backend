package com.posterpro.api.payment;

import com.posterpro.api.common.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Everything the Razorpay Checkout SDK on the client needs to open the
 * payment sheet. Note there is no client-editable amount here — the client
 * only ever displays what the server already decided to charge.
 */
@Getter
@AllArgsConstructor
public class PaymentCheckoutResponse {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private PlanTier planTier;
}
