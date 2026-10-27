package com.bcsystems.siga.auth;

import com.bcsystems.siga.domain.Persona;
import com.bcsystems.siga.domain.Rol;
import com.bcsystems.siga.domain.Token;
import com.bcsystems.siga.exception.InvalidEntryException;
import com.bcsystems.siga.repository.PersonaRepository;
import com.bcsystems.siga.repository.RolRepository;
import com.bcsystems.siga.repository.TokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final PersonaRepository personaRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RolRepository rolRepository;

    public AuthService(PersonaRepository personaRepository,
                       TokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       RolRepository rolRepository) {
        this.personaRepository = personaRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public AuthSession register(RegisterRequest request) {
        if (personaRepository.existsByUsuarioIgnoreCase(request.usuario())) {
            throw new InvalidEntryException("El usuario ya existe");
        }

        if (personaRepository.count() > 0 && !tienePermiso("PERSONAS_CREAR")) {
            throw new InvalidEntryException("Registro no autorizado");
        }

        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new InvalidEntryException("Rol no encontrado"));

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new InvalidEntryException("El rol seleccionado está inactivo");
        }

        Persona persona = Persona.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .usuario(request.usuario())
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .activa(true)
                .build();

        persona = personaRepository.save(persona);

        Token token = jwtService.generateToken(persona);
        AuthSession session = prepararToken(token, persona);

        return session;
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usuario(), request.password()
                )
        );

        Persona persona = personaRepository.findByUsuarioIgnoreCase(request.usuario())
                .orElseThrow(() -> new InvalidEntryException("Credenciales inválidas"));

        revokeAllTokens(persona);

        Token token = jwtService.generateToken(persona);
        AuthSession session = prepararToken(token, persona);

        return session;
    }

    @Transactional
    public AuthSession refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidEntryException("Token de refresco inválido");
        }

        final String username;
        try {
            username = jwtService.extractUsername(refreshToken);
            if (!"refresh".equals(jwtService.tokenType(refreshToken))) {
                throw new InvalidEntryException("Token de refresco inválido");
            }
        } catch (Exception ex) {
            throw new InvalidEntryException("Token de refresco inválido");
        }

        Persona persona = personaRepository.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new InvalidEntryException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(persona.getActiva())) {
            throw new InvalidEntryException("Usuario inactivo");
        }

        var storedToken = tokenRepository.findByRefreshTokenHash(jwtService.hashToken(refreshToken)).orElse(null);
        if (storedToken == null || storedToken.getIsRevoked() || storedToken.getIsExpired()) {
            throw new InvalidEntryException("Token de refresco inválido o expirado");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            storedToken.setIsExpired(true);
            tokenRepository.save(storedToken);
            throw new InvalidEntryException("Token de refresco expirado");
        }

        if (tokenRepository.consumeRefreshToken(storedToken.getIdToken()) == 0) {
            throw new InvalidEntryException("Token de refresco reutilizado");
        }

        revokeAllTokens(persona);

        Token newToken = jwtService.generateToken(persona);
        AuthSession session = prepararToken(newToken, persona);

        return session;
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        var storedToken = refreshToken == null ? null
                : tokenRepository.findByRefreshTokenHash(jwtService.hashToken(refreshToken)).orElse(null);
        if (storedToken == null && accessToken != null) {
            storedToken = tokenRepository.findByTokenHash(jwtService.hashToken(accessToken)).orElse(null);
        }
        if (storedToken != null) {
            storedToken.setIsRevoked(true);
            storedToken.setIsExpired(true);
            tokenRepository.save(storedToken);
        }
    }

    private void revokeAllTokens(Persona persona) {
        var tokens = tokenRepository.findAllValidTokensByPersona(persona.getIdPersona());
        tokens.forEach(t -> {
            t.setIsExpired(true);
            t.setIsRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }

    private boolean tienePermiso(String permiso) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permiso));
    }

    private AuthSession prepararToken(Token token, Persona persona) {
        String accessToken = token.getToken();
        String refreshToken = token.getRefreshToken();
        token.setTokenHash(jwtService.hashToken(accessToken));
        token.setRefreshTokenHash(jwtService.hashToken(refreshToken));
        token.setToken(null);
        token.setRefreshToken(null);
        tokenRepository.save(token);
        return new AuthSession(buildAuthResponse(persona), accessToken, refreshToken);
    }

    public AuthResponse sesion(String usuario) {
        Persona persona = personaRepository.findByUsuarioIgnoreCase(usuario)
                .orElseThrow(() -> new InvalidEntryException("Usuario no encontrado"));
        return buildAuthResponse(persona);
    }

    private AuthResponse buildAuthResponse(Persona persona) {
        var permisos = persona.getRol().getPermisos().stream()
                .map(com.bcsystems.siga.domain.Permiso::getClave)
                .collect(Collectors.toSet());
        if (persona.getPermisosAdicionales() != null) {
            persona.getPermisosAdicionales().stream()
                    .map(pa -> pa.getPermiso().getClave())
                    .forEach(permisos::add);
        }
        return new AuthResponse(
                persona.getUsuario(),
                persona.getNombre() + " " + persona.getApellido(),
                persona.getRol().getNombre(),
                permisos.stream().sorted().collect(Collectors.toList())
        );
    }
}
