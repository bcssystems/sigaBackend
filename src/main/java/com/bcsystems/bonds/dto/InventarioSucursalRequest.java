package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record InventarioSucursalRequest(
    @NotNull Integer idSucursal,
    @NotNull java.math.BigDecimal stock,
    java.math.BigDecimal stockMinimo,
    java.math.BigDecimal stockMaximo
) {}
