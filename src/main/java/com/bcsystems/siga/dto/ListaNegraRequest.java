package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotNull;

public record ListaNegraRequest(
        @NotNull Boolean enListaNegra,
        String motivo
) {}