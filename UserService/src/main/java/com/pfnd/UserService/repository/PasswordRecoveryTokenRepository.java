package com.pfnd.UserService.repository;

import com.pfnd.UserService.model.postgresql.PasswordResetToken;
import com.pfnd.UserService.model.postgresql.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByRecoveryToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
}