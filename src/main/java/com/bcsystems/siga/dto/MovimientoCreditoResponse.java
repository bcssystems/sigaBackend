package com.bcsystems.siga.dto;

import com.bcsystems.siga.domain.en.TipoMovimientoCredito;

import java.time.LocalDateTime;

public record MovimientoCreditoResponse(
        Integer idMovimiento,
        Integer idCredito,
        TipoMovimientoCredito tipo,
        Double monto,
        Double saldoAnterior,
        Double saldoNuevo,
        String descripcion,
        LocalDateTime fecha,
        String usuario,
        String metodoPago
) {}
