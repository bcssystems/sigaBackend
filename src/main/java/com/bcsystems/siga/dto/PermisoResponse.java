package com.bcsystems.siga.dto;

public record PermisoResponse(
    Integer idPermiso,
    String clave,
    String nombre,
    String descripcion,
    String modulo
) {}