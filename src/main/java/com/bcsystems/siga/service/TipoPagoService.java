package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.TipoPagoRequest;
import com.bcsystems.siga.dto.TipoPagoResponse;

import java.util.List;

public interface TipoPagoService {
    List<TipoPagoResponse> listarTodos();
    TipoPagoResponse obtenerPorId(Integer id);
    TipoPagoResponse crear(TipoPagoRequest request);
    TipoPagoResponse actualizar(Integer id, TipoPagoRequest request);
    void eliminar(Integer id);
}
