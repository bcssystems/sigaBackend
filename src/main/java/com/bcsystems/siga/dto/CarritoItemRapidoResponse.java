package com.bcsystems.siga.dto;

public record CarritoItemRapidoResponse(
        Integer idItemRapido,
        Integer idCaja,
        String descripcion,
        Double precioVenta,
        Double precioCompra,
        java.math.BigDecimal cantidad
) {}
