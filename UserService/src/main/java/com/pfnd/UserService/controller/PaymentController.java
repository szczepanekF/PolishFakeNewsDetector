package com.pfnd.UserService.controller;

import com.pfnd.UserService.model.dto.PaymentConfirmationDto;
import com.pfnd.UserService.model.dto.PaymentRequestDto;
import com.pfnd.UserService.model.postgresql.Payment;
import com.pfnd.UserService.model.response.Response;
import com.pfnd.UserService.repository.PaymentRepository;
import com.pfnd.UserService.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;  //TODO implement user service responsible for handling edge cases

    @PostMapping("/initiate")
    public ResponseEntity<Response<?>>  initiatePayment(@RequestBody PaymentRequestDto request) {
        return null;
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Response<?>>  getPaymentStatus(@PathVariable String paymentId) {
        return null;
    }


    @PostMapping("/confirm")
    public ResponseEntity<Response<?>> confirmPayment(@RequestBody PaymentConfirmationDto confirmation) {
        return null;
    }


    @GetMapping("/history/{email}")
    public ResponseEntity<Response<?>>  getUserPaymentHistory(@PathVariable String email) {
        return null;
    }

    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<Response<?>>  reundPayment(@PathVariable String paymentId) {
        return null;
    }
    // TODOv
}
