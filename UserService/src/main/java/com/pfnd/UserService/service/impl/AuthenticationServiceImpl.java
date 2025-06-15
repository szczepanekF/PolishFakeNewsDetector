package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.dto.UserDto;
import com.pfnd.UserService.model.exception.*;
import com.pfnd.UserService.model.postgresql.PasswordResetToken;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.repository.PasswordRecoveryTokenRepository;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.AuthenticationService;
import com.pfnd.UserService.service.JwtService;
import com.pfnd.UserService.utils.Consts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public String loginWithGoogle(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("emailAddress", user.getEmail());
        extraClaims.put("userId", user.getId());
        return jwtService.generateToken(extraClaims, user);
    }


    @Override
    public AuthenticationResponse registerUser(RegisterUserDto request) {
        if (isEmailOrUsernameJustExist(request.getEmail())) {
            throw new EmailOrUsernameAlreadyExistException("User with given email address already exists.");
        }
        User user = createUser(request);
        log.info("{} {}", "Successfully registered user:", new UserDto(user));
        return createAuthenticationResponse(user);
    }

    @Override
    public AuthenticationResponse loginUser(LoginUserDto request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(
                                          () -> new UserNotFoundException(
                                                  "User with this email address does not exist"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        return createAuthenticationResponse(user);
    }

    private boolean isEmailOrUsernameJustExist(String email) {
        return userRepository.findByEmail(email).isPresent()
                || userRepository.findByUsername(email.split("@")[0]).isPresent();
    }

    private User createUser(RegisterUserDto request) {
        User user = User.builder()
                        .email(request.getEmail())
                        .username(request.getEmail().split("@")[0])
                        .password(passwordEncoder.encode(request.getPassword()))
                        .createdAt(new Date())
                        .modifiedAt(new Date())
                        .isActive(false)
                        .build();

        try {
            user = userRepository.saveAndFlush(user);
        } catch (Exception e) {
            log.error("{}{}", Consts.SAVE_ERROR, user, e);
            throw new SaveException(Consts.SAVE_ERROR + user);
        }
        return userRepository.save(user);
    }

    private AuthenticationResponse createAuthenticationResponse(User user) {
        Map<String, Object> extraClaims = getUserClaims(user);

        var jwtToken = jwtService.generateToken(extraClaims, user);
        return new AuthenticationResponse(jwtToken);
    }

    private Map<String, Object> getUserClaims(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("emailAddress", user.getEmail());
        extraClaims.put("userId", user.getId());
        return extraClaims;
    }

    public String createPasswordRecoveryToken(String userEmailAddress) {
        try {
            User user = userRepository
                    .findByEmail(userEmailAddress)
                    .orElseThrow(() -> new UserNotFoundException("User with this email address does not exist"));
            Optional<PasswordResetToken> existingResetToken = passwordRecoveryTokenRepository.findByUser(user);
            Date currentDate = new Date();
            if (existingResetToken.isPresent()) {
                if (existingResetToken.get().getExpirationDate().compareTo(currentDate) > 0) {
                    return existingResetToken.get().getRecoveryToken();
                } else {
                    passwordRecoveryTokenRepository.delete(existingResetToken.get());
                }
            }
            String tokenString = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(tokenString, user);
            passwordRecoveryTokenRepository.save(resetToken);
            return tokenString;
        } catch (UserNotFoundException e) {
            log.warn("Password recovery requested for non-existing email: {}", userEmailAddress);
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error while creating password recovery token", e);
            throw new InternalServerException("Could not create recovery token at this time. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error in createPasswordRecoveryToken", e);
            throw new InternalServerException("Unexpected error while processing your request.");
        }
    }

    @Override
    public void changePassword(String token, String newPassword)  {
        try {
            PasswordResetToken resetToken = passwordRecoveryTokenRepository.findByRecoveryToken(token)
                                                                           .orElseThrow(() -> new InvalidPasswordException("Invalid or expired password reset token"));

            if (resetToken.getExpirationDate().before(new Date())) {
                passwordRecoveryTokenRepository.delete(resetToken); // clean up expired token
                throw new InvalidPasswordException("Token has expired");
            }

            User user = resetToken.getUser();

//            // You may want to validate newPassword format here (e.g., length, complexity)
//            if (!isValidPassword(newPassword)) {
//                throw new InvalidPasswordException("Password does not meet security requirements");
//            }

            // Update and save the new password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Invalidate the token after use
            passwordRecoveryTokenRepository.delete(resetToken);

        } catch (InvalidPasswordException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while changing password", e);
            throw new InternalServerException("Could not change password at this time. Please try again later.");
        }
    }

}
