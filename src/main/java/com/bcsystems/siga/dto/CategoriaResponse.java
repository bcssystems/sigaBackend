package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Integer idCategoria,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
