package com.bcsystems.siga.dto;

public record SucursalResponse(
    Integer idSucursal,
    String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
