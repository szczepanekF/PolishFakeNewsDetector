package com.pfnd.UserService.controller;

import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.dto.UserDto;
import com.pfnd.UserService.model.enums.EmailType;
import com.pfnd.UserService.model.exception.EmailOrUsernameAlreadyExistException;
import com.pfnd.UserService.model.exception.InvalidPasswordException;
import com.pfnd.UserService.model.exception.SaveException;
import com.pfnd.UserService.model.exception.UserNotFoundException;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.model.response.Response;
import com.pfnd.UserService.service.AuthenticationService;
import com.pfnd.UserService.service.MailingService;
import com.pfnd.UserService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pfnd.UserService.utils.Consts.REQUEST_EXCEPTION;

@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final MailingService mailingService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user and sends a welcome email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "400", description = "Bad request or save exception occurred"),
            @ApiResponse(responseCode = "453", description = "Messaging error while sending email"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Response<?>> register(@RequestBody RegisterUserDto request) {
        AuthenticationResponse authenticationResponse = null;
        try {
            authenticationResponse = authenticationService.registerUser(request);
            //TODO mail notifications, logging, custom exceptions

            Map<String, String> emailParameters = new HashMap<>() {{
                put("emailAddress", request.getEmail());
                put("subject", "Greeting");
                put("appname", "Polish Fake News Detector");
            }};
            mailingService.sendEmail(EmailType.NEW_ACCOUNT, emailParameters);
            return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(authenticationResponse));
        } catch (EmailOrUsernameAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response<>(e.getMessage()));
        } catch (SaveException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(e.getMessage()));
        } catch (MessagingException e) {
            return ResponseEntity.status(453).body(new Response<>(e.getMessage(), authenticationResponse));
        } catch (Exception e) {
            log.error(REQUEST_EXCEPTION, e);
            return ResponseEntity.status(500).body(new Response<>(e.getMessage()));
        }

    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Response<?>> login(@RequestBody LoginUserDto request) {
        try {
            AuthenticationResponse authenticationResponse = authenticationService.loginUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(authenticationResponse));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new Response<>(e.getMessage()));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new Response<>(e.getMessage()));
        } catch (Exception e) {
            log.error(REQUEST_EXCEPTION, e);
            return ResponseEntity.status(500).body(new Response<>(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Response<?>> getAllUsers() {
        try {
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(new Response<>(users));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new Response<>(e.getMessage()));
        }
    }

    @GetMapping(value = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get user by email",
            description = "Returns a single user based on the provided email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Response<?>> getUserDetailsByEmail(@Parameter(description = "user email") @PathVariable String email) {
        try {
            UserDto user = userService.getUserByEmail(email);
            return ResponseEntity.ok(new Response<>(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(e.getMessage()));
        } catch (Exception e) {
            log.error(REQUEST_EXCEPTION, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(e.getMessage()));
        }
    }

    @PostMapping("/passwordRecovery")
    @Operation(
            summary = "Recover user password",
            description = "Generates a password recovery token and sends a recovery email to the user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recovery email successfully sent"),
            @ApiResponse(responseCode = "404", description = "User with provided email not found"),
            @ApiResponse(responseCode = "453", description = "Error generating token or sending email"),
            @ApiResponse(responseCode = "417", description = "Expectation failed due to unexpected error")
    })

    public ResponseEntity<Response<?>> resetPassword(@RequestParam("emailAddress") String userEmailAddress) {
        try {
            String recoveryToken = authenticationService.createPasswordRecoveryToken(userEmailAddress);
            Map<String, String> emailParameters = new HashMap<>() {{
                put("token", recoveryToken);
                put("emailAddress", userEmailAddress);
                put("subject", "Recover password");
                put("appname", "Polish Fake News Detector");
            }};
            mailingService.sendEmail(EmailType.RESET_PASSWORD, emailParameters);
            return ResponseEntity.ok(new Response<>("", "Mail sent to " + userEmailAddress));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new Response<>(e.getMessage()));

        } catch (MessagingException e) {
            return ResponseEntity.status(453)
                                 .body(new Response<>(e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error while sending password recovery email", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                                 .body(new Response<>(e.getMessage(),
                                         "Operation of creating recovery token or mail sending failed"));
        }
    }


    @PostMapping("/changePassword")
    @Operation(
            summary = "Change user password",
            description = "Changes the user password using a valid recovery token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Response<?>> changePassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {
        try {
            authenticationService.changePassword(token, newPassword);
            return ResponseEntity.ok(new Response<>("Password changed successfully"));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new Response<>(e.getMessage()));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new Response<>(e.getMessage()));
        }
    }

}
