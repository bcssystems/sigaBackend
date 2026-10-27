package com.bcsystems.bonds.repository;

import com.bcsystems.bonds.domain.CuentaFinanciera;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, Integer> {
    List<CuentaFinanciera> findByActivaTrueOrderByNombreAsc();
}
