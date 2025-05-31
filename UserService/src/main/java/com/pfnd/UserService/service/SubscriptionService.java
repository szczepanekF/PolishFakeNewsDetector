package com.pfnd.UserService.service;

import com.pfnd.UserService.model.dto.SubscriptionPlanDto;
import com.pfnd.UserService.model.dto.SubscriptionStatusDto;
import com.pfnd.UserService.model.postgresql.Subscription;

import java.util.List;

public interface SubscriptionService {

    Subscription subscribe(String email, String planId);
    SubscriptionStatusDto getSubscriptionStatus(String email);
    SubscriptionPlanDto getPlanDetails(String planId);
    List<SubscriptionPlanDto> getAllPlans();
    void cancelSubscription(String email);
}
