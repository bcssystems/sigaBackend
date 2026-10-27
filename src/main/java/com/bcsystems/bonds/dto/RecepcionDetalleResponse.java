package com.bcsystems.bonds.dto;

public record RecepcionDetalleResponse(
        Integer idRecepcionDetalle,
        Integer idProducto,
        String productoNombre,
        String sku,
        java.math.BigDecimal metros,
        java.math.BigDecimal rollos,
        Double precioCompra,
        Double subtotal
) {}