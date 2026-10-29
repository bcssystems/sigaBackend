package com.bcsystems.siga.dto;

import java.util.List;

public record ProductoVentaResponse(
    Integer idProducto,
    String sku,
    String nombre,
    Double precioBase,
    java.math.BigDecimal stockActual,
    Double costoPromedio,
    Boolean activo,
    String unidadMedida,
    List<InventarioSucursalResponse> inventarioSucursales
) {
    public record InventarioSucursalResponse(Integer id, Integer idSucursal, String sucursalNombre, java.math.BigDecimal stock, java.math.BigDecimal stockMinimo, java.math.BigDecimal stockMaximo) {}
}
