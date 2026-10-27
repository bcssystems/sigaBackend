package com.bcsystems.bonds.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoResponse(
    Integer idProducto,
    String sku,
    String nombre,
    String descripcion,
    Double precioBase,
    java.math.BigDecimal stockActual,
    java.math.BigDecimal stockMinimo,
    java.math.BigDecimal stockMaximo,
    Double costoPromedio,
    Integer idCategoria,
    String categoriaNombre,
    String unidadMedida,
    Double metrosPorRollo,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, java.math.BigDecimal stock, java.math.BigDecimal stockMinimo, java.math.BigDecimal stockMaximo) {}
}