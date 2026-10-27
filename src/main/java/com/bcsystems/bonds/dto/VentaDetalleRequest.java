package com.bcsystems.bonds.dto;

public record VentaDetalleRequest(
        Integer idProducto,
        String descripcion,
        java.math.BigDecimal cantidad,
        Double precioUnitario,
        Double subtotal,
        String atributosText
) {}
