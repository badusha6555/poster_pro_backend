package com.posterpro.api.payment;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component("razorpay")
public class RazorpayProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(RazorpayProvider.class);

    private final PaymentProperties.Razorpay props;
    private final RestTemplate restTemplate = new RestTemplate();

    public RazorpayProvider(PaymentProperties properties) {
        this.props = properties.getRazorpay();
    }

    @Override
    public String name() {
        return "razorpay";
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentSession createSession(PaymentSessionRequest request) {
        long amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(props.getKeyId(), props.getKeySecret());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "amount", amountInPaise,
                "currency", request.getCurrency(),
                "receipt", "sub_" + request.getSubscriptionId()
        );

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/orders",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (RestClientException e) {
            // Never leak Razorpay's raw error body (may echo request details) to the
            // client — log full detail server-side, surface a generic failure.
            log.error("Razorpay order creation failed for user {}: {}", request.getUserId(), e.getMessage());
            throw new PaymentProviderException("Payment provider is currently unavailable", e);
        }

        Map<String, Object> resp = response.getBody();
        String orderId = resp != null ? (String) resp.get("id") : null;
        if (!response.getStatusCode().is2xxSuccessful() || orderId == null) {
            log.error("Razorpay order creation returned unexpected response for user {}: {}", request.getUserId(), resp);
            throw new PaymentProviderException("Payment provider is currently unavailable", null);
        }
        return PaymentSession.builder()
                .sessionId(orderId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .keyId(props.getKeyId())
                .build();
    }

    @Override
    @SneakyThrows
    public boolean verify(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(props.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] expected = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        byte[] actual;
        try {
            actual = HexFormat.of().parseHex(signature);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return MessageDigest.isEqual(expected, actual);
    }

    @Override
    @SneakyThrows
    public boolean verifyWebhookSignature(byte[] rawBody, String signatureHeader) {
        if (rawBody == null || signatureHeader == null) {
            return false;
        }
        String webhookSecret = props.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // Fail closed: without a configured webhook secret we cannot verify
            // authenticity, so never treat the payload as trusted.
            return false;
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] expected = mac.doFinal(rawBody);
        byte[] actual;
        try {
            actual = HexFormat.of().parseHex(signatureHeader);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return MessageDigest.isEqual(expected, actual);
    }
}
