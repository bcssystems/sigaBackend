package com.bcsystems.siga.dto;

public record ProductoVentaProjection(
    Integer idProducto,
    String sku,
    String nombre,
    Double precioBase,
    java.math.BigDecimal stockActual,
    Double costoPromedio,
    Boolean activo,
    String unidadMedida,
    Double metrosPorRollo
) {
}