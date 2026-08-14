package com.posterpro.api.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserIdOrderByEndsAtDesc(Long userId);
    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndsAtDesc(Long userId, SubscriptionStatus status);
}
