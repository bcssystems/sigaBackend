package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.RecepcionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecepcionDetalleRepository extends JpaRepository<RecepcionDetalle, Integer> {
    List<RecepcionDetalle> findByRecepcionIdRecepcion(Integer idRecepcion);

    List<RecepcionDetalle> findByRecepcionSucursalIdSucursal(Integer idSucursal);
}