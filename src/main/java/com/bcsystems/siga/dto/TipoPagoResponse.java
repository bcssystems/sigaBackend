package com.bcsystems.siga.dto;

public record TipoPagoResponse(
        Integer idTipoPago,
        String nombre,
        Boolean activo,
        Integer idCuentaFinanciera,
        String cuentaFinanciera
) {}
