package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.CotizacionRequest;
import com.bcsystems.siga.dto.CotizacionResponse;

import java.util.List;

public interface CotizacionService {
    CotizacionResponse crear(CotizacionRequest request);
    CotizacionResponse actualizar(Integer id, CotizacionRequest request);
    CotizacionResponse obtenerPorId(Integer id);
    List<CotizacionResponse> listarTodas();
    List<CotizacionResponse> listarPorEstado(String estado);
    CotizacionResponse convertirAVenta(Integer id);
    CotizacionResponse cancelar(Integer id);
}
