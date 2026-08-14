package com.posterpro.api.poster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Rate overrides from the "Set Rates" screen shown right before generating —
 * any field left null falls back to the shop's saved GoldRateProfile value.
 */
@Getter
@Setter
@NoArgsConstructor
public class PosterGenerateRequest {
    private BigDecimal rate22k916;
    private BigDecimal rate18k;
    private BigDecimal rate14k;
    private BigDecimal rate9k;
}
