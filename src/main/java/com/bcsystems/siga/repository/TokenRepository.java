package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("SELECT t FROM Token t WHERE t.persona.idPersona = :personaId AND t.isExpired = false AND t.isRevoked = false")
    List<Token> findAllValidTokensByPersona(Integer personaId);

    Optional<Token> findByTokenHash(String tokenHash);
    Optional<Token> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("UPDATE Token t SET t.isRevoked = true, t.isExpired = true "
            + "WHERE t.idToken = :id AND t.isRevoked = false AND t.isExpired = false")
    int consumeRefreshToken(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Token t SET t.isRevoked = true, t.isExpired = true "
            + "WHERE t.persona.idPersona = :personaId AND t.isRevoked = false AND t.isExpired = false")
    int revokeAllByPersonaId(@Param("personaId") Integer personaId);
}
