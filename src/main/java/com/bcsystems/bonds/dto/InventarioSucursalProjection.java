package com.bcsystems.bonds.dto;

public record InventarioSucursalProjection(
    Integer id,
    Integer idProducto,
    Integer idSucursal,
    String sucursalNombre,
    java.math.BigDecimal stock,
    java.math.BigDecimal stockMinimo,
    java.math.BigDecimal stockMaximo
) {
}