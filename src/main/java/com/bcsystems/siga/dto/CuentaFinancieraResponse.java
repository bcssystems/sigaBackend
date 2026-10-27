package com.bcsystems.siga.dto;

public record CuentaFinancieraResponse(Integer idCuenta, String nombre, String tipo, Double saldo, Boolean activa) {}
