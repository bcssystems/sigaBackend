package com.bcsystems.siga.dto;

public record CorteDetallePagoDto(
        Integer idTipoPago,
        String tipoPagoNombre,
        Double monto,
        Double montoReal
) {}
