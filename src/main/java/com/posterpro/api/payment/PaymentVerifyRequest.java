package com.posterpro.api.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What the Razorpay Checkout SDK hands back to the client on success.
 * The client's role ends at forwarding these three fields — it never
 * reports "payment succeeded" as a fact the backend trusts; only a valid
 * signature (verified server-side against razorpay_key_secret) does that.
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentVerifyRequest {
    @NotBlank
    private String razorpayOrderId;
    @NotBlank
    private String razorpayPaymentId;
    @NotBlank
    private String razorpaySignature;
}
