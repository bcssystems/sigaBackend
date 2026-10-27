package com.bcsystems.siga.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PersonaResponse(
    Integer idPersona,
    String nombre,
    String apellido,
    String usuario,
    RolResponse rol,
    Boolean activa,
    LocalDateTime fechaRegistro,
    List<Integer> permisosAdicionales
) {}