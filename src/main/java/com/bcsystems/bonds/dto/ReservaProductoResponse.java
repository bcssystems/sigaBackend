package com.bcsystems.bonds.dto;

public record ReservaProductoResponse(
        Integer idReserva,
        Integer idCaja,
        Integer idProducto,
        java.math.BigDecimal cantidad
) {}
