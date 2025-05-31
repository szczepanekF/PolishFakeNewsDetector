package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.dto.SubscriptionPlanDto;
import com.pfnd.UserService.model.dto.SubscriptionStatusDto;
import com.pfnd.UserService.model.postgresql.Subscription;
import com.pfnd.UserService.repository.SubscriptionRepository;
import com.pfnd.UserService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription subscribe(String email, String planId) {
        return null;
    }

    @Override
    public SubscriptionStatusDto getSubscriptionStatus(String email) {
        return null;
    }

    @Override
    public SubscriptionPlanDto getPlanDetails(String planId) {
        return null;
    }

    @Override
    public List<SubscriptionPlanDto> getAllPlans() {
        return List.of();
    }

    @Override
    public void cancelSubscription(String email) {

    }
}
