package com.bcsystems.bonds.dto;

import jakarta.validation.constraints.NotNull;

public record RecepcionDetalleRequest(
        @NotNull Integer idProducto,
        @NotNull @jakarta.validation.constraints.DecimalMin(value = "0.01") java.math.BigDecimal metros,
        @NotNull Double precioCompra
) {}