package com.bcsystems.bonds.service;

import com.bcsystems.bonds.domain.CorteCaja;
import com.bcsystems.bonds.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface FinanzasService {
    List<CuentaFinancieraResponse> listarCuentas();
    CuentaFinancieraResponse crearCuenta(CuentaFinancieraRequest request);
    CuentaFinancieraResponse actualizarCuenta(Integer id, CuentaFinancieraRequest request);
    void desactivarCuenta(Integer id);
    FinanzasResumenResponse resumen(Integer idCuenta, String tipo, String categoria,
                                    LocalDateTime inicio, LocalDateTime fin);
    Page<MovimientoFinancieroResponse> movimientos(Integer idCuenta, String tipo, String categoria,
                                                   LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
    void registrarCorte(CorteCaja corte);
    void registrarCompra(Integer idRecepcion, Double monto, Integer idTipoPago, Integer idSucursal, String folio);
    void registrarCancelacionVenta(Integer idVenta, Integer idSucursal);
}
