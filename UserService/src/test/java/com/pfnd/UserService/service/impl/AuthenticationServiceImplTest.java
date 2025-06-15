package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.dto.LoginUserDto;
import com.pfnd.UserService.model.dto.RegisterUserDto;
import com.pfnd.UserService.model.exception.*;
import com.pfnd.UserService.model.postgresql.PasswordResetToken;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.model.response.AuthenticationResponse;
import com.pfnd.UserService.repository.PasswordRecoveryTokenRepository;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --------- registerUser tests ---------

    @Test
    void registerUser_whenEmailOrUsernameExists_shouldThrowException() {
        RegisterUserDto request = new RegisterUserDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        // Mock repository to return user for email check
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.registerUser(request))
                .isInstanceOf(EmailOrUsernameAlreadyExistException.class)
                .hasMessageContaining("User with given email address already exists.");
    }

    @Test
    void registerUser_whenSaveThrowsException_shouldThrowSaveException() {
        RegisterUserDto request = new RegisterUserDto();
        request.setEmail("newuser@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Simulate saveAndFlush throws exception
        when(userRepository.saveAndFlush(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> authenticationService.registerUser(request))
                .isInstanceOf(SaveException.class)
                .hasMessageContaining("Failed to save an entity");
    }

    @Test
    void registerUser_success_shouldReturnAuthenticationResponse() {
        RegisterUserDto request = new RegisterUserDto();
        request.setEmail("newuser@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Simulate saveAndFlush and save return the same user with ID
        User savedUser = User.builder()
                             .id(1)
                             .email(request.getEmail())
                             .username("newuser")
                             .password("encodedPassword")
                             .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");

        verify(userRepository).saveAndFlush(any());
        verify(userRepository).save(any());
    }

    // --------- loginUser tests ---------

    @Test
    void loginUser_whenUserNotFound_shouldThrowUserNotFoundException() {
        LoginUserDto loginRequest = new LoginUserDto();
        loginRequest.setEmail("notfound@example.com");
        loginRequest.setPassword("password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.loginUser(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with this email address does not exist");
    }

    @Test
    void loginUser_whenPasswordInvalid_shouldThrowInvalidPasswordException() {
        LoginUserDto loginRequest = new LoginUserDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("wrongpassword");

        User user = User.builder()
                        .password("encodedPassword")
                        .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.loginUser(loginRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void loginUser_success_shouldReturnAuthenticationResponse() {
        LoginUserDto loginRequest = new LoginUserDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("correctpassword");

        User user = User.builder()
                        .id(1)
                        .email(loginRequest.getEmail())
                        .password("encodedPassword")
                        .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.loginUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    // --------- loginWithGoogle tests ---------

    @Test
    void loginWithGoogle_shouldReturnJwtToken() {
        User user = User.builder()
                        .id(1)
                        .email("googleuser@example.com")
                        .build();

        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("jwt-token-google");

        String token = authenticationService.loginWithGoogle(user);

        assertThat(token).isEqualTo("jwt-token-google");
    }

    // --------- createPasswordRecoveryToken tests ---------

    @Test
    void createPasswordRecoveryToken_whenUserNotFound_shouldThrowUserNotFoundException() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.createPasswordRecoveryToken(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with this email address does not exist");
    }

    @Test
    void createPasswordRecoveryToken_whenExistingTokenValid_shouldReturnExistingToken() {
        String email = "user@example.com";
        User user = User.builder().id(1).email(email).build();

        PasswordResetToken token = new PasswordResetToken();
        token.setRecoveryToken("existing-token");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1); // expiration in future
        token.setExpirationDate(cal.getTime());
        token.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordRecoveryTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        String recoveryToken = authenticationService.createPasswordRecoveryToken(email);

        assertThat(recoveryToken).isEqualTo("existing-token");

        verify(passwordRecoveryTokenRepository, never()).save(any());
        verify(passwordRecoveryTokenRepository, never()).delete(any());
    }

    @Test
    void createPasswordRecoveryToken_whenExistingTokenExpired_shouldCreateNewToken() {
        String email = "user@example.com";
        User user = User.builder().id(1).email(email).build();

        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setRecoveryToken("expired-token");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1); // expired in past
        expiredToken.setExpirationDate(cal.getTime());
        expiredToken.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordRecoveryTokenRepository.findByUser(user)).thenReturn(Optional.of(expiredToken));

        // simulate delete called for expired token
        doNothing().when(passwordRecoveryTokenRepository).delete(expiredToken);

        // simulate save returns the token object
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        when(passwordRecoveryTokenRepository.save(tokenCaptor.capture())).thenAnswer(
                invocation -> invocation.getArgument(0));

        String recoveryToken = authenticationService.createPasswordRecoveryToken(email);

        assertThat(recoveryToken).isNotBlank();
        assertThat(recoveryToken).isNotEqualTo("expired-token");
        verify(passwordRecoveryTokenRepository).delete(expiredToken);
        verify(passwordRecoveryTokenRepository).save(any());
    }

    @Test
    void createPasswordRecoveryToken_whenDataAccessException_shouldThrowInternalServerException() {
        String email = "user@example.com";
        User user = User.builder().id(1).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordRecoveryTokenRepository.findByUser(user)).thenThrow(new DataAccessException("DB error") {
        });

        assertThatThrownBy(() -> authenticationService.createPasswordRecoveryToken(email))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Could not create recovery token at this time");
    }
}

