package com.pfnd.UserService.repository;

import com.pfnd.UserService.model.postgresql.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
