package com.pfnd.UserService.service;

import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.response.AuthenticationResponse;

public interface AuthenticationService {

    String loginWithGoogle(User user);

    AuthenticationResponse registerUser(RegisterUserDto request);

    AuthenticationResponse loginUser(LoginUserDto request);

    String createPasswordRecoveryToken(String userEmailAddress);

    void changePassword(String token, String newPassword);
}

