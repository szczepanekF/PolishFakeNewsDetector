package com.pfnd.UserService.controller;

import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.model.response.Response;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.AuthenticationService;
import com.pfnd.UserService.service.SubscriptionService;
import com.pfnd.UserService.service.UserService;
import com.pfnd.UserService.utils.Consts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;  //TODO implement user service responsible for handling edge cases
    private final UserService userService;  //TODO implement user service responsible for handling edge cases
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> register(@RequestBody RegisterUserDto request) {
        try {
            AuthenticationResponse authenticationResponse = authenticationService.registerUser(request);
            //TODO mail notifications, logging, custom exceptions
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(Consts.C201, 201, "", authenticationResponse));
        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(new Response<>(e.getMessage(), 500, Arrays.toString(e.getStackTrace()), null));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<Response<?>> login(@RequestBody LoginUserDto request) {
        try {
            AuthenticationResponse authenticationResponse = authenticationService.loginUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(Consts.C201, 201, "", authenticationResponse));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new Response<>(e.getMessage(), 500, Arrays.toString(e.getStackTrace()), null));
        }
    }
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")  //TODO to be implemented
    })
    public ResponseEntity<Response<?>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(Consts.C201, 201, "", userRepository.retrieveAll()));
    }

    @GetMapping(value = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user", description = "Get user from database by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")  //TODO to be implemented
    })
    public ResponseEntity<Response<?>> getUserDetailsByEmailOrUsername(@Parameter(description = "user email") @PathVariable String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(Consts.C201, 201, "", userRepository.findByEmail(email)));
    }


    @PutMapping(value = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user", description = "Get user from database by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found")  //TODO to be implemented
    })
    public ResponseEntity<Response<?>> updateUser(@Parameter(description = "user email") @PathVariable String email) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Response<>(Consts.C201, 201, "", userRepository.findByEmail(email)));
    }
}
