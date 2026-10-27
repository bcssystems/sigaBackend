package com.bcsystems.siga.dto;

public record CategoriaRequest(
        Integer idCategoria,
        String nombre,
        String descripcion,
        Boolean activo
) {}
