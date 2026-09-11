package com.posterpro.api.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posterpro.api.common.PlanTier;
import com.posterpro.api.subscription.Subscription;
import com.posterpro.api.subscription.SubscriptionRepository;
import com.posterpro.api.subscription.SubscriptionStatus;
import com.posterpro.api.user.User;
import com.posterpro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentProviderFactory providerFactory;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentCheckoutResponse checkout(String email, PlanTier tier) {
        if (tier == PlanTier.FREE) {
            throw new IllegalArgumentException("The FREE plan does not require a payment");
        }
        User user = findUser(email);
        BigDecimal amount = paymentProperties.amountFor(tier);
        String currency = paymentProperties.getCurrency();

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setProvider(ProviderType.RAZORPAY);
        payment.setPlanTier(tier);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        PaymentSession session = providerFactory.getActiveProvider().createSession(
                PaymentSessionRequest.builder()
                        .userId(user.getId())
                        .subscriptionId(payment.getId())
                        .amount(amount)
                        .currency(currency)
                        .description("PosterPro " + tier + " subscription")
                        .build()
        );

        payment.setProviderSessionId(session.getSessionId());
        paymentRepository.save(payment);

        return new PaymentCheckoutResponse(
                payment.getId(), session.getSessionId(), session.getKeyId(), amount, currency, tier);
    }

    @Transactional
    public PaymentVerifyResponse verify(String email, PaymentVerifyRequest request) {
        User user = findUser(email);
        Payment payment = paymentRepository.findByProviderSessionId(request.getRazorpayOrderId())
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unknown payment order: " + request.getRazorpayOrderId()));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // Already verified (e.g. the webhook beat the client to it) — idempotent replay.
            Subscription subscription = payment.getSubscription();
            return new PaymentVerifyResponse(true, payment.getPlanTier(),
                    subscription != null ? subscription.getEndsAt() : null);
        }

        boolean valid = providerFactory.getActiveProvider().verify(
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature());

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Razorpay signature verification failed for order {} (user {})",
                    request.getRazorpayOrderId(), email);
            throw new IllegalArgumentException("Payment verification failed");
        }

        Subscription subscription = activateSubscription(user, payment.getPlanTier());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setSubscription(subscription);
        paymentRepository.save(payment);

        return new PaymentVerifyResponse(true, payment.getPlanTier(), subscription.getEndsAt());
    }

    /**
     * Server-to-server confirmation, independent of the client ever calling
     * {@link #verify}. This is the durable source of truth Razorpay
     * recommends relying on — the app could crash or lose connectivity right
     * after payment, and the webhook is what activates the subscription
     * anyway. Idempotent: replays for an already-SUCCESS payment are no-ops.
     */
    @Transactional
    public void handleWebhook(byte[] rawBody, String signatureHeader) {
        if (!providerFactory.getActiveProvider().verifyWebhookSignature(rawBody, signatureHeader)) {
            log.warn("Rejected Razorpay webhook call with invalid or missing signature");
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed webhook payload");
        }

        String event = root.path("event").asText("");
        if (!"payment.captured".equals(event) && !"order.paid".equals(event)) {
            log.info("Ignoring Razorpay webhook event: {}", event);
            return;
        }

        String orderId = root.path("payload").path("payment").path("entity").path("order_id").asText(null);
        if (orderId == null) {
            log.warn("Razorpay webhook '{}' payload missing order_id", event);
            return;
        }

        Payment payment = paymentRepository.findByProviderSessionId(orderId).orElse(null);
        if (payment == null) {
            log.warn("Razorpay webhook '{}' for unknown order {}", event, orderId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Razorpay webhook '{}' for already-activated order {} — ignoring", event, orderId);
            return;
        }

        Subscription subscription = activateSubscription(payment.getUser(), payment.getPlanTier());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setSubscription(subscription);
        paymentRepository.save(payment);
        log.info("Activated subscription {} via Razorpay webhook for payment {}", subscription.getId(), payment.getId());
    }

    private Subscription activateSubscription(User user, PlanTier tier) {
        LocalDateTime now = LocalDateTime.now();
        Subscription existingActive = subscriptionRepository
                .findFirstByUserIdAndStatusOrderByEndsAtDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .filter(s -> s.getEndsAt().isAfter(now))
                .orElse(null);

        LocalDateTime endsAt = (existingActive != null ? existingActive.getEndsAt() : now).plusMonths(1);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanTier(tier);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsAt(now);
        subscription.setEndsAt(endsAt);
        return subscriptionRepository.save(subscription);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }
}
