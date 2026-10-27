package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.SucursalRequest;
import com.bcsystems.siga.dto.SucursalResponse;

import java.util.List;

public interface SucursalService {
    List<SucursalResponse> listarTodas();
    SucursalResponse obtenerPorId(Integer id);
    SucursalResponse crear(SucursalRequest request);
    SucursalResponse actualizar(Integer id, SucursalRequest request);
    void eliminar(Integer id);
}
