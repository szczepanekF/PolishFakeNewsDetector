package com.pfnd.UserService.service;

import com.pfnd.UserService.model.dto.UserDto;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;

import java.util.List;

public interface UserService {

    UserDto getUserByEmail(String emailOrUsername);
    List<UserDto> getAllUsers();
}
