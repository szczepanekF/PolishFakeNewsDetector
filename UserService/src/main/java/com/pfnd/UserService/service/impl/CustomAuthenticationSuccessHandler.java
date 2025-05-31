package com.pfnd.UserService.service.impl;

import com.pfnd.UserService.model.exception.UserNotFoundException;
import com.pfnd.UserService.model.postgresql.User;
import com.pfnd.UserService.repository.UserRepository;
import com.pfnd.UserService.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();

        User user;
        if (userRepository.findByEmail(email).isEmpty()) {

            Date currentDate = new Date();
            user = User.builder()
                    .email(email)
                    .username(email.split("@")[0])
                    .password("")
                    .createdAt(currentDate)
                    .modifiedAt(currentDate)
                    .isActive(false)
                    .build();
            userRepository.save(user);
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new UserNotFoundException("User with this username or email address does not exist"));
        }
        String token = authenticationService.loginWithGoogle(user);
        //TODO redirect url
        String targetUrl = "http://localhost:8080/app/user";
        System.out.println(token);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
