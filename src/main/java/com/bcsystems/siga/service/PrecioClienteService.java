package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.PrecioClienteRequest;
import com.bcsystems.siga.dto.PrecioClienteResponse;

import java.util.List;

public interface PrecioClienteService {
    List<PrecioClienteResponse> listarPorCliente(Integer idCliente);

    List<PrecioClienteResponse> guardarPreciosCliente(Integer idCliente, List<PrecioClienteRequest> precios);

    void eliminarPrecio(Integer idPrecioCliente);

    Double obtenerPrecioEspecial(Integer idCliente, Integer idProducto);
}