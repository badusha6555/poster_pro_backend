package com.posterpro.api.poster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Rate overrides from the "Set Rates" screen shown right before generating —
 * any field left null falls back to the shop's saved GoldRateProfile value
 * where one exists (see resolution in PosterService#resolveTextValue).
 * The app sends one entry per "rate*" placeholder in the template's own
 * schema_json, which can vary per template — ignoreUnknown so a template
 * whose schema declares a rate field this DTO doesn't (yet) know about
 * fails soft (silently unused) rather than 400ing the whole request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class PosterGenerateRequest {
    private BigDecimal rate22k916;
    private BigDecimal rate18k;
    private BigDecimal rate14k;
    private BigDecimal rate9k;
    // No corresponding GoldRateProfile column yet — request-only overrides.
    private BigDecimal rate22k8g;
    private BigDecimal rate18k8g;
}
