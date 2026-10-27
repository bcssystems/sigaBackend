package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record KardexUnificadoResponse(
    Long id,
    LocalDateTime fecha,
    String tipo,
    String entidad,
    String nombre,
    String sku,
    String sucursalNombre,
    String detalle,
    String usuario,
    String referencia,
    java.math.BigDecimal cantidad,
    java.math.BigDecimal stockAnterior,
    java.math.BigDecimal stockNuevo,
    String origen
) {}