package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.GastoRequest;
import com.bcsystems.siga.dto.GastoResponse;

import java.util.List;

public interface GastoService {
    GastoResponse solicitar(GastoRequest request);
    List<GastoResponse> pendientes();
    List<GastoResponse> listarTodos();
    List<GastoResponse> listarPorCaja(Integer idCaja);
    long contarPendientesPorCaja(Integer idCaja);
    GastoResponse autorizar(Integer idGasto);
    GastoResponse rechazar(Integer idGasto);
}
