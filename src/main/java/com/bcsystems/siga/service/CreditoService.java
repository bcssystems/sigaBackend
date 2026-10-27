package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.*;

import java.util.List;

public interface CreditoService {

    List<CreditoResponse> listarCreditosPorCliente(Integer idCliente);

    List<MovimientoCreditoResponse> listarMovimientosPorCliente(Integer idCliente);

    AbonoResponse registrarAbono(AbonoRequest request);

    List<AbonoResponse> abonarATodas(AbonoGeneralRequest request);

    EstadoCuentaResponse estadoCuenta(Integer idCredito);
}
