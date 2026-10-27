package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.Caja;
import com.bcsystems.bonds.domain.InventarioSucursal;
import com.bcsystems.bonds.domain.ReservaProducto;
import com.bcsystems.bonds.dto.ReservaProductoResponse;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.CajaRepository;
import com.bcsystems.bonds.repository.InventarioSucursalRepository;
import com.bcsystems.bonds.repository.ReservaProductoRepository;
import com.bcsystems.bonds.repository.VentaDetalleRepository;
import com.bcsystems.bonds.service.ReservaProductoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaProductoServiceImpl implements ReservaProductoService {

    private final ReservaProductoRepository reservaProductoRepository;
    private final CajaRepository cajaRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final com.bcsystems.bonds.repository.ProductoRepository productoRepository;

    public ReservaProductoServiceImpl(ReservaProductoRepository reservaProductoRepository,
                                      CajaRepository cajaRepository,
                                      InventarioSucursalRepository inventarioSucursalRepository,
                                      VentaDetalleRepository ventaDetalleRepository,
                                      com.bcsystems.bonds.repository.ProductoRepository productoRepository) {
        this.reservaProductoRepository = reservaProductoRepository;
        this.cajaRepository = cajaRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public void reservar(Integer idCaja, Integer idProducto, java.math.BigDecimal cantidad) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));

        validarCantidadEnteraSiUnidad(idProducto, cantidad);
        validarStockDisponible(idProducto, caja.getSucursal().getIdSucursal(), idCaja, cantidad);

        ReservaProducto reserva = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .orElse(ReservaProducto.builder()
                        .caja(caja)
                        .idProducto(idProducto)
                        .build());

        reserva.setCantidad(cantidad);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setExpiraEn(LocalDateTime.now().plusMinutes(30));
        reservaProductoRepository.save(reserva);
    }

    @Override
    @Transactional
    public void quitarReserva(Integer idCaja, Integer idProducto) {
        reservaProductoRepository.findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .ifPresent(reservaProductoRepository::delete);
    }

    @Override
    @Transactional
    public void actualizarCantidad(Integer idCaja, Integer idProducto, java.math.BigDecimal cantidad) {
        ReservaProducto reserva = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .orElseThrow(() -> new NotFoundException("No hay reserva activa para este producto"));

        Caja caja = reserva.getCaja();
        validarCantidadEnteraSiUnidad(idProducto, cantidad);
        validarStockDisponible(idProducto, caja.getSucursal().getIdSucursal(), idCaja, cantidad);

        reserva.setCantidad(cantidad);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setExpiraEn(LocalDateTime.now().plusMinutes(30));
        reservaProductoRepository.save(reserva);
    }

    @Override
    @Transactional
    public void limpiarReservas(Integer idCaja) {
        reservaProductoRepository.deleteByCajaIdCaja(idCaja);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaProductoResponse> obtenerReservasPorSucursal(Integer idSucursal) {
        return reservaProductoRepository.findByCajaSucursalIdSucursal(idSucursal)
                .stream()
                .map(r -> new ReservaProductoResponse(r.getIdReserva(), r.getCaja().getIdCaja(),
                        r.getIdProducto(), r.getCantidad()))
                .toList();
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void limpiarExpiradas() {
        List<ReservaProducto> expiradas = reservaProductoRepository
                .findByExpiraEnBefore(LocalDateTime.now());
        if (!expiradas.isEmpty()) {
            reservaProductoRepository.deleteAll(expiradas);
        }
    }

    private void validarCantidadEnteraSiUnidad(Integer idProducto, java.math.BigDecimal cantidad) {
        // Las cantidades de todos los productos admiten valores decimales.
    }

    private void validarStockDisponible(Integer idProducto, Integer idSucursal,
                                         Integer idCajaExcluir, java.math.BigDecimal nuevaCantidad) {
        java.math.BigDecimal cero = java.math.BigDecimal.ZERO;
        java.math.BigDecimal reservadoPorOtros = reservaProductoRepository
                .sumCantidadReservada(idProducto, idSucursal, LocalDateTime.now());
        if (reservadoPorOtros == null) reservadoPorOtros = cero;

        java.math.BigDecimal reservadoPorEstaCaja = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCajaExcluir, idProducto)
                .map(r -> r.getCantidad() != null ? r.getCantidad() : cero)
                .orElse(cero);

        java.math.BigDecimal totalReservado = reservadoPorOtros
                .subtract(reservadoPorEstaCaja)
                .add(nuevaCantidad)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        InventarioSucursal inv = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursal)
                .orElse(null);

        java.math.BigDecimal stockDisponible = inv != null && inv.getStock() != null
                ? inv.getStock() : cero;

        java.math.BigDecimal cantidadEnEspera = ventaDetalleRepository
                .sumCantidadEnEspera(idProducto, idSucursal);
        if (cantidadEnEspera != null) {
            stockDisponible = stockDisponible.add(cantidadEnEspera);
        }

        if (totalReservado.compareTo(stockDisponible) > 0) {
            throw new InvalidEntryException("Stock insuficiente en la sucursal"
                    + " (disponible: " + stockDisponible
                    + ", solicitado: " + totalReservado + ")");
        }
    }
}
