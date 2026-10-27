package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record MovimientoStockResponse(
    Integer idMovimiento,
    Integer idProducto,
    String productoSku,
    String productoNombre,
    Integer idSucursal,
    String sucursalNombre,
    String tipoMovimiento,
    java.math.BigDecimal cantidad,
    java.math.BigDecimal stockAnterior,
    java.math.BigDecimal stockNuevo,
    String referencia,
    String usuario,
    String observacion,
    LocalDateTime fechaMovimiento
) {}
