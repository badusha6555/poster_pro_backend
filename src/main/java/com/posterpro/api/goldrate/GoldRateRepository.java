package com.posterpro.api.goldrate;

import com.posterpro.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoldRateRepository extends JpaRepository<GoldRateProfile, Long> {
    Optional<GoldRateProfile> findByUser(User user);
}
