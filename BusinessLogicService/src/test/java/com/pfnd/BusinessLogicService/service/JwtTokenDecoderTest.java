package com.pfnd.BusinessLogicService.service;

import com.pfnd.BusinessLogicService.service.impl.JwtTokenDecoderImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtTokenDecoderTest {

    private JwtTokenDecoderImpl decoder;
    private String secret;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        secret = Base64.getEncoder().encodeToString(key.getEncoded());

        decoder = new JwtTokenDecoderImpl();

        var secretField = JwtTokenDecoderImpl.class.getDeclaredFields()[0];
        secretField.setAccessible(true);
        try {
            secretField.set(decoder, secret);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bearer ", ""})
    void testDecodeValidToken(String tokenPrefix) {
        String token = Jwts.builder()
                           .subject("testuser")
                           .claim("role", "USER")
                           .issuedAt(new Date())
                           .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                           .compact();

        Claims claims = decoder.decode(tokenPrefix + token);

        assertEquals("testuser", claims.getSubject());
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void testDecodeInvalidFormatToken() {
        String malformedToken = "not-a.jwt";

        JwtException ex = assertThrows(JwtException.class, () -> {
            decoder.decode("Bearer " + malformedToken);
        });

        assertEquals("Invalid JWT token", ex.getMessage());
    }

    @Test
    void testDecodeTamperedToken() {
        String token = Jwts.builder()
                           .subject("user")
                           .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                           .compact();

        String tampered = token + "123";

        assertThrows(JwtException.class, () -> {
            decoder.decode("Bearer " + tampered);
        });
    }
}