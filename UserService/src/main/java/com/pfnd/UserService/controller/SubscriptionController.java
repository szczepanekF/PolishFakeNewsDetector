package com.pfnd.UserService.controller;

import com.pfnd.UserService.model.dto.SubscriptionRequestDto;
import com.pfnd.UserService.model.response.Response;
import com.pfnd.UserService.repository.SubscriptionRepository;
import com.pfnd.UserService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<Response<?>>  subscribe(@RequestBody SubscriptionRequestDto request) {
        return null;
    }

    @GetMapping("/status/{email}")
    public ResponseEntity<Response<?>>  getSubscriptionStatus(@PathVariable String email) {
        return null;
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<Response<?>>  getPlanDetails(@PathVariable String planId) {
        return null;
    }

    @GetMapping("/plans")
    public ResponseEntity<Response<?>>  listAllPlans() {
        return null;
    }

    @PostMapping("/cancel/{email}")
    public ResponseEntity<Response<?>>  cancelSubscription(@PathVariable String email) {
        return null;
    }
}
