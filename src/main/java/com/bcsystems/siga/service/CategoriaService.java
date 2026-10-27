package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.CategoriaRequest;
import com.bcsystems.siga.dto.CategoriaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {
    Page<CategoriaResponse> listar(String search, Boolean activo, Pageable pageable);
    List<CategoriaResponse> listarActivas();
    CategoriaResponse crear(CategoriaRequest request);
    CategoriaResponse actualizar(Integer id, CategoriaRequest request);
    void eliminar(Integer id);
}
