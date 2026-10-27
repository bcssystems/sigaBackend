package com.bcsystems.siga.auth;

import com.bcsystems.siga.domain.Persona;
import com.bcsystems.siga.repository.PersonaRepository;
import com.bcsystems.siga.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final PersonaRepository personaRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   TokenRepository tokenRepository,
                                   PersonaRepository personaRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh-token")
                || path.equals("/api/v1/auth/logout") || path.equals("/api/v1/auth/csrf")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = cookieValue(request, "SIGA_ACCESS");
        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token", "Token is malformed");
            return;
        }

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Persona persona = personaRepository.findByUsuarioIgnoreCase(username).orElse(null);
        if (persona == null || !persona.getActiva()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token", "User not found for token");
            return;
        }

        var storedToken = tokenRepository.findByTokenHash(jwtService.hashToken(jwt)).orElse(null);
        if (storedToken == null || storedToken.getIsRevoked() || storedToken.getIsExpired()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "token_invalid", "Token is invalid or revoked");
            return;
        }

        if (!"access".equals(jwtService.tokenType(jwt)) || !jwtService.isTokenValid(jwt, persona)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "token_expired", "Token has expired");
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private String cookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + error + "\", \"message\": \"" + message + "\"}");
    }
}
