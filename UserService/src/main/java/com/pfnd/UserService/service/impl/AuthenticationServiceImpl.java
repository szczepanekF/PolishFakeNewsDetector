package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.exception.EmailOrUsernameAlreadyExistException;
import com.pfnd.UserService.model.exception.UserNotFoundException;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.AuthenticationService;
import com.pfnd.UserService.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
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
            throw new EmailOrUsernameAlreadyExistException("Email or username just exist");
        }
        User user = createUser(request);
        System.out.println(user);
        return createAuthenticationResponse(user);
    }

    @Override
    public AuthenticationResponse loginUser(LoginUserDto request) {
        String usernameOrEmail = request.getUsernameOrEmail();
        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("User with this username or email address does not exist"));

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
}
