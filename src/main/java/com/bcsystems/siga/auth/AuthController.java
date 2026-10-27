package com.bcsystems.siga.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean secureCookies;
    private final String sameSite;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthService authService,
                          @Value("${app.security.cookies.secure:false}") boolean secureCookies,
                          @Value("${app.security.cookies.same-site:Lax}") String sameSite,
                          LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response,
                                              HttpServletRequest httpRequest) {
        String key = httpRequest.getRemoteAddr() + ":" + request.usuario().trim().toLowerCase();
        if (!loginRateLimiter.isAllowed(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        try {
            AuthSession session = authService.login(request);
            loginRateLimiter.clear(key);
            return authenticatedResponse(session, response);
        } catch (RuntimeException ex) {
            loginRateLimiter.recordFailure(key);
            throw ex;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {
        return authenticatedResponse(authService.register(request), response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(value = "SIGA_REFRESH", required = false) String refreshToken,
            HttpServletResponse response) {
        return authenticatedResponse(authService.refreshToken(refreshToken), response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "SIGA_ACCESS", required = false) String accessToken,
            @CookieValue(value = "SIGA_REFRESH", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(accessToken, refreshToken);
        deleteCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.sesion(userDetails.getUsername()));
    }

    @GetMapping("/csrf")
    public ResponseEntity<String> csrf(CsrfToken token) {
        return ResponseEntity.ok("{\"token\":\"" + token.getToken() + "\"}");
    }

    private ResponseEntity<AuthResponse> authenticatedResponse(AuthSession session, HttpServletResponse response) {
        addCookie(response, "SIGA_ACCESS", session.accessToken(), "/", 900);
        addCookie(response, "SIGA_REFRESH", session.refreshToken(), "/api/v1/auth", 604800);
        return ResponseEntity.ok(session.response());
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .httpOnly(true).secure(secureCookies).sameSite(sameSite).path(path).maxAge(maxAge).build().toString());
    }

    private void deleteCookies(HttpServletResponse response) {
        addCookie(response, "SIGA_ACCESS", "", "/", 0);
        addCookie(response, "SIGA_REFRESH", "", "/api/v1/auth", 0);
    }
}
