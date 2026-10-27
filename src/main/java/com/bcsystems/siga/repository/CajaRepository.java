package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.Caja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CajaRepository extends JpaRepository<Caja, Integer> {
    List<Caja> findByActivaTrueOrderByNombre();
    List<Caja> findBySucursalIdSucursalAndActivaTrue(Integer idSucursal);
}
