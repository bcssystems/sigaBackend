package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Optional<Persona> findByUsuarioIgnoreCase(String usuario);
    Optional<Persona> findByUsuario(String usuario);
    boolean existsByUsuarioIgnoreCase(String usuario);
    boolean existsByUsuarioIgnoreCaseAndIdPersonaNot(String usuario, Integer idPersona);

    Page<Persona> findByActiva(Boolean activa, Pageable pageable);

    @Query("""
        SELECT p FROM Persona p
        LEFT JOIN p.rol r
        WHERE (:activa IS NULL OR p.activa = :activa)
        AND (:texto IS NULL OR :texto = '' OR
             LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR
             LOWER(p.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) OR
             LOWER(p.usuario) LIKE LOWER(CONCAT('%', :texto, '%')) OR
             LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
    """)
    Page<Persona> buscar(@org.springframework.data.repository.query.Param("activa") Boolean activa,
                         @org.springframework.data.repository.query.Param("texto") String texto,
                         Pageable pageable);

    @Query("select p from Persona p where p.rol is null")
    java.util.List<Persona> findPendientesMigracionRol();
}
