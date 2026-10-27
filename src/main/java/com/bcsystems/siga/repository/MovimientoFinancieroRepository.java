package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.MovimientoFinanciero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface MovimientoFinancieroRepository extends JpaRepository<MovimientoFinanciero, Integer> {
    boolean existsByOrigenAndIdOrigen(String origen, Integer idOrigen);
    Optional<MovimientoFinanciero> findFirstByOrigenAndIdOrigenOrderByIdMovimientoAsc(String origen, Integer idOrigen);

    @Query("""
        SELECT m FROM MovimientoFinanciero m
        WHERE (:idCuenta IS NULL OR m.cuenta.idCuenta = :idCuenta)
        AND (:tipo IS NULL OR m.tipo = :tipo)
        AND (:categoria IS NULL OR m.categoria = :categoria)
        AND (:inicio IS NULL OR m.fecha >= :inicio)
        AND (:fin IS NULL OR m.fecha <= :fin)
        ORDER BY m.fecha DESC, m.idMovimiento DESC
    """)
    Page<MovimientoFinanciero> buscar(@Param("idCuenta") Integer idCuenta,
                                      @Param("tipo") String tipo,
                                      @Param("categoria") String categoria,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin,
                                      Pageable pageable);
}
