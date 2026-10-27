package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotNull;

public record VentaPagoRequest(
        @NotNull Integer idTipoPago,
        @NotNull Double monto,
        String referencia
) {}
