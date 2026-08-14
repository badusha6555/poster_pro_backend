package com.posterpro.api.payment;

import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@Component("razorpay")
public class RazorpayProvider implements PaymentProvider {

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

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.razorpay.com/v1/orders",
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> resp = response.getBody();
        return PaymentSession.builder()
                .sessionId((String) resp.get("id"))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .keyId(props.getKeyId())
                .build();
    }

    @Override
    @SneakyThrows
    public boolean verify(String orderId, String paymentId, String signature) {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(props.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash).equals(signature);
    }
}
