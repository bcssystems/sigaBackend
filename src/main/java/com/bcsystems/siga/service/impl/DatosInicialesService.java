package com.bcsystems.siga.service.impl;

import com.bcsystems.siga.domain.Caja;
import com.bcsystems.siga.domain.Sucursal;
import com.bcsystems.siga.domain.en.CajaEstado;
import com.bcsystems.siga.domain.en.TipoCaja;
import com.bcsystems.siga.repository.CajaRepository;
import com.bcsystems.siga.repository.SucursalRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatosInicialesService {

    private final SucursalRepository sucursalRepository;
    private final CajaRepository cajaRepository;

    public DatosInicialesService(SucursalRepository sucursalRepository,
                                 CajaRepository cajaRepository) {
        this.sucursalRepository = sucursalRepository;
        this.cajaRepository = cajaRepository;
    }

    @PostConstruct
    @Transactional
    public void inicializar() {
        Sucursal sucursal = sucursalRepository.findByActivaTrueOrderByNombreAsc().stream()
                .findFirst()
                .orElse(null);
        if (sucursal == null && sucursalRepository.count() == 0) {
            sucursal = sucursalRepository.save(Sucursal.builder()
                    .nombre("Sucursal Principal")
                    .direccion("")
                    .telefono("")
                    .activa(true)
                    .build());
        }
        if (sucursal == null) {
            return;
        }

        boolean existeNormal = cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .anyMatch(c -> c.getTipo() == TipoCaja.NORMAL);
        if (!existeNormal) {
            cajaRepository.save(Caja.builder()
                    .nombre("Caja Principal")
                    .tipo(TipoCaja.NORMAL)
                    .sucursal(sucursal)
                    .estado(CajaEstado.CERRADA)
                    .saldoActual(0.0)
                    .activa(true)
                    .build());
        }

        boolean existeChica = cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .anyMatch(c -> c.getTipo() == TipoCaja.CHICA);
        if (!existeChica) {
            cajaRepository.save(Caja.builder()
                    .nombre("Caja Chica")
                    .tipo(TipoCaja.CHICA)
                    .sucursal(sucursal)
                    .estado(CajaEstado.CERRADA)
                    .saldoActual(0.0)
                    .activa(true)
                    .build());
        }
    }
}