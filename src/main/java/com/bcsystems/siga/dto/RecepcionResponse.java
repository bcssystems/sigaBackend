package com.bcsystems.siga.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecepcionResponse(
        Integer idRecepcion,
        String folio,
        Integer idProveedor,
        String proveedorNombre,
        String proveedorRfc,
        Integer idSucursal,
        String sucursalNombre,
        Integer idUsuario,
        String usuario,
        java.math.BigDecimal totalMetros,
        String nota,
        LocalDateTime fechaRecepcion,
        List<RecepcionDetalleResponse> detalles
) {}
