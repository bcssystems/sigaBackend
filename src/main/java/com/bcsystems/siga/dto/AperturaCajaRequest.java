package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotNull;

public record AperturaCajaRequest(
        @NotNull Double saldoInicial
) {}
