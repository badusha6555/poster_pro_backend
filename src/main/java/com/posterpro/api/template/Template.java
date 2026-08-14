package com.posterpro.api.template;

import com.posterpro.api.category.Category;
import com.posterpro.api.common.PlanTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "templates")
@Getter
@Setter
@NoArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "background_image_key")
    private String backgroundImageKey;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "schema_json", columnDefinition = "jsonb")
    private String schemaJson;

    @Column(name = "is_festival")
    private Boolean isFestival;

    @Column(name = "festival_date")
    private LocalDate festivalDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier_min", nullable = false)
    private PlanTier planTierMin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
