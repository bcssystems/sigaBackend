package com.bcsystems.siga.service;

import com.bcsystems.siga.dto.CancelarVentaRequest;
import com.bcsystems.siga.dto.CodigoAutorizacionResponse;
import com.bcsystems.siga.dto.VentaEsperaRequest;
import com.bcsystems.siga.dto.VentaPagoRequest;
import com.bcsystems.siga.dto.VentaRequest;
import com.bcsystems.siga.dto.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    VentaResponse crear(VentaRequest request);
    VentaResponse obtenerPorId(Integer id);
    List<VentaResponse> listarPorCaja(Integer idCaja);
    Page<VentaResponse> listar(Integer idSucursal, Integer idCaja, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    VentaResponse cancelar(Integer id, CancelarVentaRequest request);
    CodigoAutorizacionResponse generarCodigo(Integer id);
    VentaResponse solicitarCancelacion(Integer id, String motivo);
    VentaResponse rechazarCancelacion(Integer id);
    VentaResponse ponerEnEspera(Integer id);
    VentaResponse reanudar(Integer id);
    VentaResponse cancelarEspera(Integer id);
    VentaResponse actualizarEspera(Integer id, VentaEsperaRequest request);
    List<VentaResponse> ventasEnEspera(Integer idCaja);
    List<VentaResponse> listarPorSucursal(Integer idSucursal);
    VentaResponse registrarPago(Integer id, List<VentaPagoRequest> pagos);
    VentaResponse ventaRapida(Integer idCaja, String descripcion, Double precioCompra, Double precioVenta, java.math.BigDecimal cantidad, Integer idCliente);
}
