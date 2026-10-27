package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record MovimientoFinancieroResponse(Integer idMovimiento, Integer idCuenta, String cuenta,
                                           Integer idTipoPago, String tipoPago, String tipo,
                                           String categoria, Double monto, String origen,
                                           Integer idOrigen, String descripcion, String usuario,
                                           String sucursal, LocalDateTime fecha) {}
