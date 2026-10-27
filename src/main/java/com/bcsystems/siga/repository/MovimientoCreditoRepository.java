package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.MovimientoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoCreditoRepository extends JpaRepository<MovimientoCredito, Integer> {

    List<MovimientoCredito> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);

    List<MovimientoCredito> findByCreditoClienteIdClienteOrderByFechaDesc(Integer idCliente);
}
