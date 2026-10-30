package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.CatalogoCategoriaResponse;
import com.bcsystems.siga.dto.CatalogoProductoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CatalogoService {
    List<CatalogoCategoriaResponse> listarCategorias();
    Page<CatalogoProductoResponse> listarProductos(String search, Integer idCategoria, Pageable pageable);
    CatalogoProductoResponse obtenerProducto(Integer idProducto);
}
