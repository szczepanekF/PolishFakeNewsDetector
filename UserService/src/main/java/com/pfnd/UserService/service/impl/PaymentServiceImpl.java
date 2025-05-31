package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.dto.PaymentConfirmationDto;
import com.pfnd.UserService.model.dto.PaymentRequestDto;
import com.pfnd.UserService.model.dto.PaymentStatusDto;
import com.pfnd.UserService.model.postgresql.Payment;
import com.pfnd.UserService.repository.PaymentRepository;
import com.pfnd.UserService.repository.SubscriptionRepository;
import com.pfnd.UserService.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    @Override
    public String initiatePayment(PaymentRequestDto request) {
        return "";
    }

    @Override
    public PaymentStatusDto getStatus(String paymentId) {
        return null;
    }

    @Override
    public void confirmPayment(PaymentConfirmationDto confirmation) {

    }

    @Override
    public List<Payment> getPaymentHistory(UUID userId) {
        return List.of();
    }

    @Override
    public void refundPayment(UUID paymentId) {

    }
}
