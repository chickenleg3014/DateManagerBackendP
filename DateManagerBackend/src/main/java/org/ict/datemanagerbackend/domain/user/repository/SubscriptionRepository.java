package org.ict.datemanagerbackend.domain.user.repository;

import org.ict.datemanagerbackend.domain.user.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
