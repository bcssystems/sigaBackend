package com.bcsystems.bonds.dto;

import java.util.List;

public record FinanzasResumenResponse(Double total, Double efectivo, Double bancos,
                                      Double ingresos, Double egresos, Double ventas,
                                      Double abonos, Double compras, Double gastos,
                                      Double creditoPendiente,
                                      List<CuentaFinancieraResponse> cuentas) {}
