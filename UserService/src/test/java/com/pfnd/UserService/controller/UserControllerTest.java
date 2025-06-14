package com.pfnd.UserService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfnd.UserService.config.TestSecurityConfig;
import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.dto.UserDto;
import com.pfnd.UserService.model.enums.EmailType;
import com.pfnd.UserService.model.exception.EmailOrUsernameAlreadyExistException;
import com.pfnd.UserService.model.exception.InvalidPasswordException;
import com.pfnd.UserService.model.exception.SaveException;
import com.pfnd.UserService.model.exception.UserNotFoundException;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.service.AuthenticationService;
import com.pfnd.UserService.service.JwtService;
import com.pfnd.UserService.service.MailingService;
import com.pfnd.UserService.service.UserService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private MailingService mailingService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterUserDto registerUserDto;
    private LoginUserDto loginUserDto;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setup() {
        registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("test@example.com");
        registerUserDto.setPassword("password123");

        loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");
        loginUserDto.setPassword("password123");

        authResponse = new AuthenticationResponse("token123");
    }

    @Test
    void registerUser_Success() throws Exception {
        when(authenticationService.registerUser(any(RegisterUserDto.class))).thenReturn(authResponse);
        doNothing().when(mailingService).sendEmail(eq(EmailType.NEW_ACCOUNT), anyMap());

        mockMvc.perform(post("/app/user/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerUserDto)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.contained_object.token").value("token123"));

        verify(authenticationService).registerUser(any(RegisterUserDto.class));
        verify(mailingService).sendEmail(eq(EmailType.NEW_ACCOUNT), anyMap());
    }

    @Test
    void registerUser_EmailOrUsernameExists() throws Exception {
        when(authenticationService.registerUser(any(RegisterUserDto.class)))
                .thenThrow(new EmailOrUsernameAlreadyExistException("Email or username already exists"));

        mockMvc.perform(post("/app/user/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerUserDto)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value("Email or username already exists"));
    }

    @Test
    void registerUser_SaveException() throws Exception {
        when(authenticationService.registerUser(any(RegisterUserDto.class)))
                .thenThrow(new SaveException("Save exception"));

        mockMvc.perform(post("/app/user/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerUserDto)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Save exception"));
    }

    @Test
    void registerUser_MessagingException() throws Exception {
        when(authenticationService.registerUser(any(RegisterUserDto.class))).thenReturn(authResponse);
        doThrow(new MessagingException("Messaging failed")).when(mailingService)
                                                           .sendEmail(eq(EmailType.NEW_ACCOUNT), anyMap());

        mockMvc.perform(post("/app/user/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(registerUserDto)))
               .andExpect(status().is(453))
               .andExpect(jsonPath("$.error").value("Messaging failed"))
               .andExpect(jsonPath("$.contained_object.token").value("token123"));
    }

    @Test
    void loginUser_Success() throws Exception {
        when(authenticationService.loginUser(any(LoginUserDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/app/user/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginUserDto)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.contained_object.token").value("token123"));
    }

    @Test
    void loginUser_UserNotFound() throws Exception {
        when(authenticationService.loginUser(any(LoginUserDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/app/user/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginUserDto)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void loginUser_InvalidPassword() throws Exception {
        when(authenticationService.loginUser(any(LoginUserDto.class)))
                .thenThrow(new InvalidPasswordException("Invalid password"));

        mockMvc.perform(post("/app/user/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(loginUserDto)))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.error").value("Invalid password"));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        UserDto user1 = new UserDto();
        user1.setEmail("user1@example.com");

        UserDto user2 = new UserDto();
        user2.setEmail("user2@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/app/user"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.contained_object").isArray())
               .andExpect(jsonPath("$.contained_object[0].email").value("user1@example.com"))
               .andExpect(jsonPath("$.contained_object[1].email").value("user2@example.com"));
    }

    @Test
    void getUserDetailsByEmail_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setEmail("user@example.com");

        when(userService.getUserByEmail("user@example.com")).thenReturn(userDto);

        mockMvc.perform(get("/app/user/user@example.com")
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.contained_object.email").value("user@example.com"));
    }

    @Test
    void getUserDetailsByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail("notfound@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/app/user/notfound@example.com")
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void resetPassword_Success() throws Exception {
        String token = "recovery-token";
        String email = "recover@example.com";

        when(authenticationService.createPasswordRecoveryToken(email)).thenReturn(token);
        doNothing().when(mailingService).sendEmail(eq(EmailType.RESET_PASSWORD), anyMap());

        mockMvc.perform(post("/app/user/passwordRecovery")
                       .param("emailAddress", email))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.contained_object").value("Mail sent to " + email));
    }

    @Test
    void resetPassword_UserNotFound() throws Exception {
        String email = "unknown@example.com";

        when(authenticationService.createPasswordRecoveryToken(email))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/app/user/passwordRecovery")
                       .param("emailAddress", email))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void resetPassword_MessagingException() throws Exception {
        String email = "recover@example.com";
        String token = "recovery-token";

        when(authenticationService.createPasswordRecoveryToken(email)).thenReturn(token);
        doThrow(new MessagingException("Mail error")).when(mailingService)
                                                     .sendEmail(eq(EmailType.RESET_PASSWORD), anyMap());

        mockMvc.perform(post("/app/user/passwordRecovery")
                       .param("emailAddress", email))
               .andExpect(status().is(453))
               .andExpect(jsonPath("$.error").value("Mail error"));
    }
}
