package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.ProductoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoMultimediaRepository extends JpaRepository<ProductoMultimedia, Integer> {
    List<ProductoMultimedia> findByProductoIdProductoOrderByOrdenAscIdMultimediaAsc(Integer idProducto);
    List<ProductoMultimedia> findByProductoIdProductoInOrderByOrdenAscIdMultimediaAsc(Collection<Integer> ids);
    List<ProductoMultimedia> findByProductoIdProductoInAndTipoOrderByEsPrincipalDescOrdenAscIdMultimediaAsc(Collection<Integer> ids, String tipo);
    long countByProductoIdProducto(Integer idProducto);
    Optional<ProductoMultimedia> findFirstByProductoIdProductoAndEsPrincipalTrueAndTipoOrderByOrdenAsc(Integer idProducto, String tipo);

    @Query("select coalesce(max(m.orden), -1) from ProductoMultimedia m where m.producto.idProducto = :idProducto")
    int maxOrden(@Param("idProducto") Integer idProducto);
}
