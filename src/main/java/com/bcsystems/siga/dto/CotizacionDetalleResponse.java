package com.bcsystems.siga.dto;

public record CotizacionDetalleResponse(
        Integer idDetalle,
        Integer idProducto,
        String productoNombre,
        String productoSku,
        java.math.BigDecimal cantidad,
        Double precioUnitario,
        Double subtotal
) {}
