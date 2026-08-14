package com.posterpro.api.goldrate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GoldRateDto {
    private BigDecimal rate22k916;
    private BigDecimal rate18k;
    private BigDecimal rate14k;
    private BigDecimal rate9k;
    private LocalDateTime updatedAt;
}
