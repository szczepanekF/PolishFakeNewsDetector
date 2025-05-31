package com.pfnd.UserService.service;

import com.pfnd.UserService.model.dto.PaymentConfirmationDto;
import com.pfnd.UserService.model.dto.PaymentRequestDto;
import com.pfnd.UserService.model.dto.PaymentStatusDto;
import com.pfnd.UserService.model.postgresql.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    String initiatePayment(PaymentRequestDto request);
    PaymentStatusDto getStatus(String paymentId);
    void confirmPayment(PaymentConfirmationDto confirmation);
    List<Payment> getPaymentHistory(UUID userId);
    void refundPayment(UUID paymentId);
}
