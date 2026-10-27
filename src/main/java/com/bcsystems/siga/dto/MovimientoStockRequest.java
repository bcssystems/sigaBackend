package com.bcsystems.siga.dto;

import com.bcsystems.siga.domain.en.TipoMovimiento;
import jakarta.validation.constraints.NotNull;

public record MovimientoStockRequest(
    @NotNull TipoMovimiento tipoMovimiento,
    @NotNull @jakarta.validation.constraints.DecimalMin(value = "0.01") java.math.BigDecimal cantidad,
    Integer idSucursal,
    String referencia,
    String observacion
) {}
