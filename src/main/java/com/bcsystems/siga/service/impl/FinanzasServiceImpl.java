package com.bcsystems.siga.service.impl;

import com.bcsystems.siga.domain.*;
import com.bcsystems.siga.dto.*;
import com.bcsystems.siga.exception.InvalidEntryException;
import com.bcsystems.siga.exception.NotFoundException;
import com.bcsystems.siga.repository.*;
import com.bcsystems.siga.service.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanzasServiceImpl implements FinanzasService {
    private final CuentaFinancieraRepository cuentaRepository;
    private final MovimientoFinancieroRepository movimientoRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final VentaPagoRepository ventaPagoRepository;
    private final AbonoPagoRepository abonoPagoRepository;
    private final AbonoRepository abonoRepository;
    private final GastoRepository gastoRepository;
    private final RecepcionRepository recepcionRepository;
    private final PersonaRepository personaRepository;
    private final SucursalRepository sucursalRepository;
    private final CreditoRepository creditoRepository;

    @Override
    public List<CuentaFinancieraResponse> listarCuentas() {
        return cuentaRepository.findAll().stream().map(this::toCuenta).toList();
    }

    @Override
    @Transactional
    public CuentaFinancieraResponse crearCuenta(CuentaFinancieraRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) throw new InvalidEntryException("El nombre es obligatorio");
        CuentaFinanciera c = CuentaFinanciera.builder().nombre(request.nombre().trim())
                .tipo(request.tipo() == null || request.tipo().isBlank() ? "OTRO" : request.tipo().toUpperCase())
                .saldo(0.0).activa(true).build();
        return toCuenta(cuentaRepository.save(c));
    }

    @Override
    @Transactional
    public CuentaFinancieraResponse actualizarCuenta(Integer id, CuentaFinancieraRequest request) {
        CuentaFinanciera c = cuentaRepository.findById(id).orElseThrow(() -> new NotFoundException("Cuenta financiera no encontrada"));
        c.setNombre(request.nombre().trim());
        if (request.tipo() != null && !request.tipo().isBlank()) c.setTipo(request.tipo().toUpperCase());
        return toCuenta(cuentaRepository.save(c));
    }

    @Override
    @Transactional
    public void desactivarCuenta(Integer id) {
        CuentaFinanciera c = cuentaRepository.findById(id).orElseThrow(() -> new NotFoundException("Cuenta financiera no encontrada"));
        c.setActiva(false);
        cuentaRepository.save(c);
    }

    @Override
    public FinanzasResumenResponse resumen(Integer idCuenta, String tipo, String categoria,
                                           LocalDateTime inicio, LocalDateTime fin) {
        List<CuentaFinanciera> cuentas = cuentaRepository.findAll();
        List<MovimientoFinanciero> movimientos = movimientoRepository.buscar(idCuenta, tipo, categoria, inicio, fin, Pageable.unpaged()).getContent();
        List<CuentaFinanciera> cuentasFiltradas = idCuenta == null ? cuentas : cuentas.stream()
                .filter(c -> idCuenta.equals(c.getIdCuenta())).toList();
        double ingresos = movimientos.stream().filter(m -> "INGRESO".equals(m.getTipo())).mapToDouble(MovimientoFinanciero::getMonto).sum();
        double egresos = movimientos.stream().filter(m -> "EGRESO".equals(m.getTipo())).mapToDouble(MovimientoFinanciero::getMonto).sum();
        double creditoPendiente = safe(creditoRepository.sumarSaldoPendiente());
        return new FinanzasResumenResponse(
                cuentasFiltradas.stream().mapToDouble(c -> c.getSaldo() == null ? 0 : c.getSaldo()).sum(),
                cuentasFiltradas.stream().filter(c -> "EFECTIVO".equals(c.getTipo())).mapToDouble(c -> safe(c.getSaldo())).sum(),
                cuentasFiltradas.stream().filter(c -> !"EFECTIVO".equals(c.getTipo())).mapToDouble(c -> safe(c.getSaldo())).sum(),
                ingresos, egresos,
                sumaCategoria(movimientos, "VENTA"), sumaCategoria(movimientos, "ABONO"),
                sumaCategoria(movimientos, "COMPRA"), sumaCategoria(movimientos, "GASTO"), creditoPendiente,
                cuentas.stream().map(this::toCuenta).toList());
    }

    @Override
    public Page<MovimientoFinancieroResponse> movimientos(Integer idCuenta, String tipo, String categoria,
                                                           LocalDateTime inicio, LocalDateTime fin, Pageable pageable) {
        return movimientoRepository.buscar(idCuenta, tipo, categoria, inicio, fin, pageable).map(this::toMovimiento);
    }

    @Override
    @Transactional
    public void registrarCorte(CorteCaja corte) {
        if (corte == null || corte.getFechaApertura() == null || corte.getFechaCierre() == null) return;
        Integer caja = corte.getCaja().getIdCaja();
        ventaPagoRepository.findByCajaAndFechaRange(caja, corte.getFechaApertura(), corte.getFechaCierre()).stream()
                .filter(p -> p.getVenta() != null && "COMPLETADA".equals(p.getVenta().getEstado().name()))
                .forEach(p -> registrarPago(p.getIdVentaPago(), p.getTipoPago(), p.getMonto(), "VENTA", "VENTA_PAGO",
                        p.getVenta().getIdVenta(), p.getVenta().getCaja().getSucursal(), "Venta #" + p.getVenta().getIdVenta()));

        abonoRepository.findByCajaIdCajaAndFechaBetweenOrderByFechaDesc(caja, corte.getFechaApertura(), corte.getFechaCierre()).forEach(a -> {
            List<AbonoPago> pagos = abonoPagoRepository.findByAbonoIdAbono(a.getIdAbono());
            if (!pagos.isEmpty()) pagos.forEach(p -> registrarPago(p.getIdAbonoPago(), p.getTipoPago(), p.getMonto(), "ABONO", "ABONO_PAGO",
                    a.getIdAbono(), null, "Abono de crédito #" + a.getIdAbono()));
        });

        gastoRepository.findByCajaIdCajaAndFechaAutorizacionBetweenOrderByFechaAutorizacionAsc(caja, corte.getFechaApertura(), corte.getFechaCierre())
                .forEach(g -> {
                    if (!movimientoRepository.existsByOrigenAndIdOrigen("GASTO", g.getIdGasto())) {
                        crearMovimiento(cuentaEfectivo(), null, "EGRESO", "GASTO", g.getMonto(), "GASTO", g.getIdGasto(),
                                g.getDescripcion(), g.getCaja().getSucursal());
                    }
                });
    }

    @Override
    @Transactional
    public void registrarCompra(Integer idRecepcion, Double monto, Integer idTipoPago, Integer idSucursal, String folio) {
        if (monto == null || monto <= 0) return;
        TipoPago tp = tipoPagoRepository.findById(idTipoPago).orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(idSucursal).orElse(null);
        registrarPago(idRecepcion, tp, monto, "COMPRA", "COMPRA", idRecepcion, sucursal, "Compra " + folio);
    }

    @Override
    @Transactional
    public void registrarCancelacionVenta(Integer idVenta, Integer idSucursal) {
        ventaPagoRepository.findByVentaIdVenta(idVenta).forEach(p -> {
            String origen = "CANCELACION_VENTA_PAGO";
            if (movimientoRepository.existsByOrigenAndIdOrigen(origen, p.getIdVentaPago())) return;
            movimientoRepository.findFirstByOrigenAndIdOrigenOrderByIdMovimientoAsc("VENTA_PAGO", p.getIdVentaPago()).ifPresent(original -> {
                crearMovimiento(original.getCuenta(), original.getTipoPago(), "EGRESO", "CANCELACION", p.getMonto(), origen,
                        p.getIdVentaPago(), "Cancelación de venta #" + idVenta, original.getSucursal());
            });
        });
    }

    private void registrarPago(Integer sourceKey, TipoPago tipoPago, Double monto, String categoria, String origen,
                               Integer idOrigen, Sucursal sucursal, String descripcion) {
        // El identificador de origen debe ser el mismo que se persiste para que
        // un corte posterior no vuelva a registrar el movimiento.
        if (movimientoRepository.existsByOrigenAndIdOrigen(origen, idOrigen)) return;
        if (tipoPago == null || tipoPago.getCuentaFinanciera() == null)
            throw new InvalidEntryException("El método de pago no tiene una cuenta financiera asignada");
        crearMovimiento(tipoPago.getCuentaFinanciera(), tipoPago, "INGRESO".equals(categoria) ? "INGRESO" :
                        ("GASTO".equals(categoria) || "COMPRA".equals(categoria) ? "EGRESO" : "INGRESO"),
                categoria, monto, origen, idOrigen, descripcion, sucursal);
    }

    private void crearMovimiento(CuentaFinanciera cuenta, TipoPago tipoPago, String tipo, String categoria, Double monto,
                                 String origen, Integer idOrigen, String descripcion, Sucursal sucursal) {
        MovimientoFinanciero m = MovimientoFinanciero.builder().cuenta(cuenta).tipoPago(tipoPago).tipo(tipo)
                .categoria(categoria).monto(Math.abs(monto == null ? 0 : monto)).origen(origen).idOrigen(idOrigen)
                .descripcion(descripcion).usuario(usuarioActual()).sucursal(sucursal).fecha(LocalDateTime.now()).build();
        movimientoRepository.save(m);
        cuenta.setSaldo(safe(cuenta.getSaldo()) + ("INGRESO".equals(tipo) ? m.getMonto() : -m.getMonto()));
        cuentaRepository.save(cuenta);
    }

    private CuentaFinanciera cuentaEfectivo() {
        return cuentaRepository.findAll().stream()
                .filter(c -> "EFECTIVO".equals(c.getTipo()) && Boolean.TRUE.equals(c.getActiva()))
                .findFirst().orElseThrow(() -> new InvalidEntryException("No existe una cuenta de efectivo activa"));
    }

    private Persona usuarioActual() {
        String name = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : "SISTEMA";
        return personaRepository.findByUsuario(name).orElse(null);
    }
    private double safe(Double d) { return d == null ? 0 : d; }
    private double sumaCategoria(List<MovimientoFinanciero> ms, String c) { return ms.stream().filter(m -> c.equals(m.getCategoria())).mapToDouble(MovimientoFinanciero::getMonto).sum(); }
    private CuentaFinancieraResponse toCuenta(CuentaFinanciera c) { return new CuentaFinancieraResponse(c.getIdCuenta(), c.getNombre(), c.getTipo(), safe(c.getSaldo()), c.getActiva()); }
    private MovimientoFinancieroResponse toMovimiento(MovimientoFinanciero m) {
        return new MovimientoFinancieroResponse(m.getIdMovimiento(), m.getCuenta().getIdCuenta(), m.getCuenta().getNombre(),
                m.getTipoPago() != null ? m.getTipoPago().getIdTipoPago() : null,
                m.getTipoPago() != null ? m.getTipoPago().getNombre() : null, m.getTipo(), m.getCategoria(), m.getMonto(),
                m.getOrigen(), m.getIdOrigen(), m.getDescripcion(), m.getUsuario() != null ? m.getUsuario().getUsuario() : "SISTEMA",
                m.getSucursal() != null ? m.getSucursal().getNombre() : null, m.getFecha());
    }
}
