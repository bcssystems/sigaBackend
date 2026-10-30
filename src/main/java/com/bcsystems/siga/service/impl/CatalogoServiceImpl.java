package com.bcsystems.siga.service.impl;

import com.bcsystems.siga.domain.Categoria;
import com.bcsystems.siga.domain.Producto;
import com.bcsystems.siga.domain.ProductoMultimedia;
import com.bcsystems.siga.dto.CatalogoCategoriaResponse;
import com.bcsystems.siga.dto.CatalogoMediaResponse;
import com.bcsystems.siga.dto.CatalogoProductoResponse;
import com.bcsystems.siga.exception.NotFoundException;
import com.bcsystems.siga.repository.CategoriaRepository;
import com.bcsystems.siga.repository.ProductoMultimediaRepository;
import com.bcsystems.siga.repository.ProductoRepository;
import com.bcsystems.siga.service.CatalogoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoServiceImpl implements CatalogoService {
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository multimediaRepository;

    public CatalogoServiceImpl(CategoriaRepository categoriaRepository,
                               ProductoRepository productoRepository,
                               ProductoMultimediaRepository multimediaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.multimediaRepository = multimediaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoCategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllActivas().stream()
                .map(c -> new CatalogoCategoriaResponse(c.getIdCategoria(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogoProductoResponse> listarProductos(String search, Integer idCategoria, Pageable pageable) {
        return productoRepository.buscarConFiltros(search, true, null, idCategoria, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoProductoResponse obtenerProducto(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        return toResponse(producto);
    }

    private CatalogoProductoResponse toResponse(Producto producto) {
        List<CatalogoMediaResponse> multimedia = multimediaRepository
                .findByProductoIdProductoOrderByOrdenAscIdMultimediaAsc(producto.getIdProducto())
                .stream().map(this::toMediaResponse).toList();
        String principal = multimedia.stream()
                .filter(m -> Boolean.TRUE.equals(m.esPrincipal()) && "IMAGEN".equals(m.tipo()))
                .map(CatalogoMediaResponse::url).findFirst()
                .orElseGet(() -> multimedia.stream().filter(m -> "IMAGEN".equals(m.tipo()))
                        .map(CatalogoMediaResponse::url).findFirst().orElse(null));
        Categoria categoria = producto.getCategoria();
        return new CatalogoProductoResponse(
                producto.getIdProducto(), producto.getSku(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecioBase(), producto.getUnidadMedida(),
                categoria != null ? categoria.getIdCategoria() : null,
                categoria != null ? categoria.getNombre() : null,
                principal, multimedia);
    }

    private CatalogoMediaResponse toMediaResponse(ProductoMultimedia media) {
        return new CatalogoMediaResponse(media.getIdMultimedia(), media.getTipo(), media.getUrl(),
                media.getNombreArchivo(), media.getEsPrincipal(), media.getOrden());
    }
}
