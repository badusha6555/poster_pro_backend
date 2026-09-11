package com.posterpro.api.payment;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponse> checkout(@Valid @RequestBody PaymentCheckoutRequest request) {
        return ResponseEntity.ok(paymentService.checkout(currentEmail(), request.getPlanTier()));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponse> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verify(currentEmail(), request));
    }

    /**
     * Public (see SecurityConfig — permitAll for this path): Razorpay's
     * servers call this directly, with no JWT. Authenticity instead comes
     * from the X-Razorpay-Signature header, verified against the raw body
     * using the webhook secret (payment.razorpay.webhook-secret) — never the
     * per-request key secret used for checkout verification.
     */
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Void> razorpayWebhook(
            HttpServletRequest request,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) throws IOException {
        byte[] rawBody = request.getInputStream().readAllBytes();
        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
