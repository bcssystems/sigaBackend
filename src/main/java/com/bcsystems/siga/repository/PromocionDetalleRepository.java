package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.PromocionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionDetalleRepository extends JpaRepository<PromocionDetalle, Integer> {
    void deleteByPromocionIdPromocion(Integer idPromocion);
}
