package com.pfnd.UserService.repository;

import com.pfnd.UserService.model.postgresql.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
}
