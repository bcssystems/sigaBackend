package com.bcsystems.siga.auth;

import com.bcsystems.siga.domain.Persona;
import com.bcsystems.siga.domain.Permiso;
import com.bcsystems.siga.domain.Token;
import com.bcsystems.siga.domain.en.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Token generateToken(Persona persona) {
        return generateToken(persona, UUID.randomUUID().toString());
    }

    public Token generateToken(Persona persona, String sessionFamily) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", persona.getNombre() + " " + persona.getApellido());
        claims.put("rol", persona.getRol().getNombre());
        claims.put("permisos", new ArrayList<>(getAllPermissions(persona)));

        claims.put("session_family", sessionFamily);
        String accessToken = buildToken(claims, persona.getUsuario(), jwtExpiration, "access");
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("session_family", sessionFamily);
        String refreshToken = buildToken(refreshClaims, persona.getUsuario(), refreshExpiration, "refresh");

        return Token.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type(TokenType.BEARER)
                .isRevoked(false)
                .isExpired(false)
                .sessionFamily(sessionFamily)
                .persona(persona)
                .build();
    }

    public String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private Set<String> getAllPermissions(Persona persona) {
        Set<String> permisos = persona.getRol().getPermisos().stream()
                .map(Permiso::getClave)
                .collect(Collectors.toSet());

        if (persona.getPermisosAdicionales() != null) {
            persona.getPermisosAdicionales().stream()
                    .map(pa -> pa.getPermiso().getClave())
                    .forEach(permisos::add);
        }

        return permisos;
    }

    public Set<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        List<String> permisos = claims.get("permisos", List.class);
        return permisos != null ? new LinkedHashSet<>(permisos) : new LinkedHashSet<>();
    }

    public String tokenType(String token) {
        return extractClaim(token, claims -> claims.get("token_type", String.class));
    }

    public String sessionFamily(String token) {
        return extractClaim(token, claims -> claims.get("session_family", String.class));
    }

    public boolean belongsToSiga(String token) {
        return "SIGA".equals(extractClaim(token, claims -> claims.get("issuer", String.class)))
                && "SIGA_API".equals(extractClaim(token, claims -> claims.get("audience", String.class)));
    }

    private String buildToken(Map<String, Object> extraClaims, String username, long expiration, String tokenType) {
        return Jwts.builder()
                .claims(extraClaims)
                .claim("issuer", "SIGA")
                .claim("audience", "SIGA_API")
                .claim("token_type", tokenType)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, Persona persona) {
        final String username = extractUsername(token);
        return (username.equals(persona.getUsuario())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
