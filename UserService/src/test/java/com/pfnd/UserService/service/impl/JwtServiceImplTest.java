package com.pfnd.UserService.service.impl;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class JwtServiceImplTest {
    private JwtServiceImpl jwtService;
    @BeforeEach
    void setup() {
        jwtService = new JwtServiceImpl();
        // Inject the base64Key manually (copy your test secret here)
        jwtService.base64Key = "Zm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFy";
        // Call the init() to set up secretKey
        jwtService.init();
    }
    @Test
    void generateToken_and_extractUsername() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");

        UserDetails userDetails = new UserDetails() {
            @Override
            public String getUsername() {
                return "testuser";
            }
            @Override public String getPassword() { return null; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
            @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
        };

        // Act
        String token = jwtService.generateToken(claims, userDetails);
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(token).isNotNull();
        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        // Arrange
        UserDetails userDetails = new UserDetails() {
            @Override public String getUsername() { return "validuser"; }
            @Override public String getPassword() { return null; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
            @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
        };

        String token = jwtService.generateToken(Collections.emptyMap(), userDetails);

        // Act
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_returnsFalseForWrongUsername() {
        // Arrange
        UserDetails userDetails = new UserDetails() {
            @Override public String getUsername() { return "user1"; }
            @Override public String getPassword() { return null; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
            @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
        };

        UserDetails differentUser = new UserDetails() {
            @Override public String getUsername() { return "user2"; }
            @Override public String getPassword() { return null; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
            @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
        };

        String token = jwtService.generateToken(Collections.emptyMap(), userDetails);

        // Act
        boolean isValid = jwtService.validateToken(token, differentUser);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_returnsTrueForExpiredToken() throws InterruptedException {
        UserDetails userDetails = new UserDetails() {
            @Override public String getUsername() { return "expireduser"; }
            @Override public String getPassword() { return null; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
            @Override public java.util.Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
        };

        String token = Jwts.builder()
                           .setSubject(userDetails.getUsername())
                           .setIssuedAt(new Date(System.currentTimeMillis()))
                           .setExpiration(new Date(System.currentTimeMillis() + 1000))
                           .signWith(jwtService.secretKey)
                           .compact();

        Thread.sleep(2000);

        boolean expired = jwtService.isTokenExpired(token);

        assertThat(expired).isTrue();
    }
}
