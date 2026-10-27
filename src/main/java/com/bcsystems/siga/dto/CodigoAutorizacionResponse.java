package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record CodigoAutorizacionResponse(
        Integer idVenta,
        String codigo,
        LocalDateTime generadoEn,
        LocalDateTime expiraEn) {
}