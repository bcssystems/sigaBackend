package com.bcsystems.siga.dto;

public record CatalogoMediaResponse(
        Integer idMultimedia,
        String tipo,
        String url,
        String nombreArchivo,
        Boolean esPrincipal,
        Integer orden
) {}
