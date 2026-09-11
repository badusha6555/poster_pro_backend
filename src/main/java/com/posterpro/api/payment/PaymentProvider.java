package com.posterpro.api.payment;

public interface PaymentProvider {
    String name();
    PaymentSession createSession(PaymentSessionRequest request);
    boolean verify(String orderId, String paymentId, String signature);

    /**
     * Verifies a server-to-server webhook call actually came from the provider,
     * using the raw (unparsed) request body — signature schemes are computed
     * over the exact bytes sent, so a re-serialized JSON object won't match.
     */
    boolean verifyWebhookSignature(byte[] rawBody, String signatureHeader);
}
