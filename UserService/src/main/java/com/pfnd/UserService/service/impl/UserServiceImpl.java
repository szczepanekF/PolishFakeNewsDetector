package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.dto.UserDto;
import com.pfnd.UserService.model.exception.UserNotFoundException;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                                  .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
        return new UserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
    }
}
