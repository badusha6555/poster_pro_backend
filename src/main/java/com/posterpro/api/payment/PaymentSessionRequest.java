package com.posterpro.api.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSessionRequest {
    private Long userId;
    private Long subscriptionId;
    private BigDecimal amount;
    private String currency;
    private String description;
}
