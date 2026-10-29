package com.bcsystems.siga.dto;

public record RecepcionDetalleResponse(
        Integer idRecepcionDetalle,
        Integer idProducto,
        String productoNombre,
        String sku,
        java.math.BigDecimal metros,
        Double precioCompra,
        Double subtotal
) {}
