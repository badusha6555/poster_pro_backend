package com.posterpro.api.goldrate;

import com.posterpro.api.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gold_rate_profiles")
@Getter
@Setter
@NoArgsConstructor
public class GoldRateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "rate_22k916", precision = 10, scale = 2)
    private BigDecimal rate22k916;

    @Column(name = "rate_18k", precision = 10, scale = 2)
    private BigDecimal rate18k;

    @Column(name = "rate_14k", precision = 10, scale = 2)
    private BigDecimal rate14k;

    @Column(name = "rate_9k", precision = 10, scale = 2)
    private BigDecimal rate9k;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
