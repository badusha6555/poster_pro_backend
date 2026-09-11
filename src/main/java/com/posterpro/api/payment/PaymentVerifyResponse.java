package com.posterpro.api.payment;

import com.posterpro.api.common.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentVerifyResponse {
    private boolean verified;
    private PlanTier planTier;
    private LocalDateTime subscriptionEndsAt;
}
