package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record TransferenciaRequest(
    @NotNull Integer idSucursalOrigen,
    @NotNull Integer idSucursalDestino,
    @NotNull @jakarta.validation.constraints.DecimalMin(value = "0.01") java.math.BigDecimal cantidad,
    String referencia,
    String observacion
) {}
