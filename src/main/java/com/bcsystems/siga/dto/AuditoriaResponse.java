package com.bcsystems.siga.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(
    Integer idAuditoria,
    String entidad,
    Integer entidadId,
    String accion,
    String usuario,
    LocalDateTime fecha,
    String detalle,
    String referencia,
    java.math.BigDecimal cantidad,
    java.math.BigDecimal stockAnterior,
    java.math.BigDecimal stockNuevo
) {}
