package com.posterpro.api.payment;

/** Thrown when the upstream payment provider (e.g. Razorpay) is unreachable or errors. */
public class PaymentProviderException extends RuntimeException {
    public PaymentProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
