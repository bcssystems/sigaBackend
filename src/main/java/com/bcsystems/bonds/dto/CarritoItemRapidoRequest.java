package com.bcsystems.bonds.dto;

public record CarritoItemRapidoRequest(
        Integer idCaja,
        String descripcion,
        Double precioVenta,
        Double precioCompra,
        java.math.BigDecimal cantidad
) {}
