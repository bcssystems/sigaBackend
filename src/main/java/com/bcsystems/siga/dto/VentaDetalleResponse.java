package com.bcsystems.siga.dto;

public record VentaDetalleResponse(
        Integer idVentaDetalle,
        Integer idProducto,
        String productoSku,
        String productoNombre,
        String descripcion,
        java.math.BigDecimal cantidad,
        String unidadMedida,
        Double precioUnitario,
        Double subtotal,
        String atributosText
) {}
