package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public AuthenticationResponse createUser() {
        return null;
    }

    @Override
    public User getUserByEmailOrUsername(String emailOrUsername) {
        return null;
    }
}
