package com.pfnd.BusinessLogicService.service.impl;

import com.pfnd.BusinessLogicService.service.JwtTokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtTokenDecoderImpl implements JwtTokenDecoder {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Claims decode(String token) {
        String jwtToken = token.replace("Bearer ", "");
        String[] parts = jwtToken.split("\\.");

        if (parts.length < 3) {
            throw new JwtException("Invalid JWT token");
        }

        return Jwts.parser()
                   .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                   .build()
                   .parseSignedClaims(jwtToken)
                   .getPayload();
    }
}
