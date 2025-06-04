package com.pfnd.BusinessLogicService.config;

import com.pfnd.BusinessLogicService.service.JwtTokenDecoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtTokenDecoder jwtDecoder;
    private final BearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = tokenResolver.resolve(request);
        System.err.println("TOKEN");
        if (token == null) {
            log.info("Received request without JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing JWT token\"}");
            return;
        }
        log.debug("Processing request's JWT token");
        try {
            Claims claims = jwtDecoder.decode(token);
            System.err.println("SZCZEPANEK " + claims.toString());

            String userId = claims.getSubject(); // or claims.get("userId")
            // Optionally: roles, expiration, etc.
            System.err.println("SZCZEPANEK " + userId);

            // Create an authentication object and set it in SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            log.info("Received request with invalid JWT token " + e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
