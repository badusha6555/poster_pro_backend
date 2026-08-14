package com.posterpro.api.payment;

public interface PaymentProvider {
    String name();
    PaymentSession createSession(PaymentSessionRequest request);
    boolean verify(String orderId, String paymentId, String signature);
}
