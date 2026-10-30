package com.bcsystems.siga.dto;

import java.util.List;

public record CatalogoProductoResponse(
        Integer idProducto,
        String sku,
        String nombre,
        String descripcion,
        Double precio,
        String unidadMedida,
        Integer idCategoria,
        String categoriaNombre,
        String imagenPrincipalUrl,
        List<CatalogoMediaResponse> multimedia
) {}
