package com.bcsystems.siga.auth;

public record AuthSession(AuthResponse response, String accessToken, String refreshToken) {
}
