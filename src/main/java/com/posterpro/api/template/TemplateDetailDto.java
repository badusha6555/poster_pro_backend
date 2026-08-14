package com.posterpro.api.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String planTierMin;
    private Boolean isFestival;
    private LocalDate festivalDate;
    private Boolean isFavorited;
    private String schemaJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
