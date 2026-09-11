package com.posterpro.api.payment;

import com.posterpro.api.common.PlanTier;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentCheckoutRequest {
    @NotNull
    private PlanTier planTier;
}
