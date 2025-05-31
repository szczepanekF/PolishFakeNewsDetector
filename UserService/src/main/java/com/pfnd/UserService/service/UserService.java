package com.pfnd.UserService.service;

import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;

public interface UserService {
    AuthenticationResponse createUser();

    User getUserByEmailOrUsername(String emailOrUsername);
}
