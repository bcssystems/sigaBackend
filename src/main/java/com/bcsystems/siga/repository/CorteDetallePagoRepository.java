package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.CorteDetallePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorteDetallePagoRepository extends JpaRepository<CorteDetallePago, Integer> {

    List<CorteDetallePago> findByCorteIdCorte(Integer idCorte);
}
