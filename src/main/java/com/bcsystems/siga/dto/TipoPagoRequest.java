package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotBlank;

public record TipoPagoRequest(
        @NotBlank String nombre,
        Integer idCuentaFinanciera
) {}
