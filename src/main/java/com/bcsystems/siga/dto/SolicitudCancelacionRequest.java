package com.bcsystems.siga.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCancelacionRequest(
        @NotBlank String motivo
) {}