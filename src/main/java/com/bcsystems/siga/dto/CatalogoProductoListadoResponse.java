package com.bcsystems.siga.dto;

public record CatalogoProductoListadoResponse(
        Integer idProducto,
        String sku,
        String nombre,
        Double precio,
        String unidadMedida,
        Integer idCategoria,
        String categoriaNombre,
        String imagenPrincipalUrl
) {}
