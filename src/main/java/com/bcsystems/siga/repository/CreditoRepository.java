package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.Credito;
import com.bcsystems.siga.domain.en.EstadoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository extends JpaRepository<Credito, Integer> {

    Optional<Credito> findByVentaIdVenta(Integer idVenta);

    List<Credito> findByClienteIdClienteAndEstadoOrderByFechaCreacionDesc(Integer idCliente, EstadoCredito estado);

    boolean existsByFolio(String folio);

    List<Credito> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);

    @Query("SELECT COALESCE(SUM(c.saldoPendiente), 0) FROM Credito c " +
           "WHERE c.estado IN (com.bcsystems.siga.domain.en.EstadoCredito.ACTIVO, " +
           "com.bcsystems.siga.domain.en.EstadoCredito.VENCIDO) " +
           "AND c.saldoPendiente > 0")
    Double sumarSaldoPendiente();

    @Query("SELECT c FROM Credito c WHERE c.cliente.idCliente = :idCliente ORDER BY c.fechaCreacion DESC")
    List<Credito> findActivosByCliente(@Param("idCliente") Integer idCliente);
}
