package com.bcsystems.bonds.service.impl;

import com.bcsystems.bonds.domain.*;
import com.bcsystems.bonds.domain.en.TipoMovimiento;
import com.bcsystems.bonds.dto.*;
import com.bcsystems.bonds.exception.InvalidEntryException;
import com.bcsystems.bonds.exception.NotFoundException;
import com.bcsystems.bonds.repository.*;
import com.bcsystems.bonds.service.AuditoriaService;
import com.bcsystems.bonds.service.RecepcionService;
import com.bcsystems.bonds.service.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecepcionServiceImpl implements RecepcionService {

    private final RecepcionRepository recepcionRepository;
    private final RecepcionDetalleRepository recepcionDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalRepository sucursalRepository;
    private final PersonaRepository personaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaService auditoriaService;
    private final FinanzasService finanzasService;

    @Override
    public List<RecepcionResponse> listar(String search, Integer idSucursal) {
        return recepcionRepository.buscar(search, idSucursal).stream()
                .map(r -> toResponse(r, recepcionDetalleRepository.findByRecepcionIdRecepcion(r.getIdRecepcion())))
                .toList();
    }

    @Override
    public List<RecepcionResponse> listarTodos() {
        return recepcionRepository.findAll().stream()
                .sorted(Comparator.comparing(Recepcion::getFechaRecepcion).reversed())
                .map(r -> toResponse(r, recepcionDetalleRepository.findByRecepcionIdRecepcion(r.getIdRecepcion())))
                .toList();
    }

    @Override
    public RecepcionResponse obtenerPorId(Integer id) {
        Recepcion recepcion = buscarOExcepcion(id);
        return toResponse(recepcion, recepcionDetalleRepository.findByRecepcionIdRecepcion(id));
    }

    @Override
    @Transactional
    public RecepcionResponse crear(RecepcionRequest request) {
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        Proveedor proveedor = request.idProveedor() != null
                ? proveedorRepository.findById(request.idProveedor()).orElse(null) : null;
        Persona persona = obtenerPersonaActual();

        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new InvalidEntryException("La recepcion debe tener al menos un detalle");
        }

        java.math.BigDecimal cero = java.math.BigDecimal.ZERO;
        Recepcion recepcion = Recepcion.builder()
                .folio(generarFolio())
                .proveedor(proveedor)
                .sucursal(sucursal)
                .usuario(persona)
                .totalMetros(cero)
                .totalRollos(cero)
                .nota(request.nota())
                .detalles(new ArrayList<>())
                .build();

        java.math.BigDecimal totalMetros = cero;
        java.math.BigDecimal totalRollos = cero;
        double totalCompra = 0.0;
        String usuarioNombre = persona.getNombre() + (persona.getApellido() != null ? " " + persona.getApellido() : "");

        for (RecepcionDetalleRequest dr : request.detalles()) {
            Producto producto = productoRepository.findById(dr.idProducto())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + dr.idProducto()));
            if (dr.metros() == null || dr.metros().compareTo(cero) <= 0) {
                throw new InvalidEntryException("Los metros deben ser mayores a cero para: " + producto.getNombre());
            }
            if (dr.precioCompra() == null || dr.precioCompra() < 0) {
                throw new InvalidEntryException("El precio de compra no puede ser negativo para: " + producto.getNombre());
            }

            double metrosPorRollo = producto.getMetrosPorRollo() != null && producto.getMetrosPorRollo() > 0
                    ? producto.getMetrosPorRollo() : 1.0;
            java.math.BigDecimal rollos = java.math.BigDecimal.valueOf(
                            Math.ceil(dr.metros().doubleValue() / metrosPorRollo))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal precioCompraBd = java.math.BigDecimal.valueOf(dr.precioCompra());

            RecepcionDetalle detalle = RecepcionDetalle.builder()
                    .recepcion(recepcion)
                    .producto(producto)
                    .metros(dr.metros())
                    .rollos(rollos)
                    .precioCompra(dr.precioCompra())
                    .subtotal(precioCompraBd.multiply(dr.metros()).doubleValue())
                    .build();
            recepcion.getDetalles().add(detalle);
            totalCompra += detalle.getSubtotal();

            totalMetros = totalMetros.add(dr.metros());
            totalRollos = totalRollos.add(rollos);

            java.math.BigDecimal stockAnterior = producto.getStockActual() != null
                    ? producto.getStockActual() : cero;
            java.math.BigDecimal stockNuevo = stockAnterior.add(dr.metros())
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            producto.setStockActual(stockNuevo);

            if (stockNuevo.compareTo(cero) > 0) {
                java.math.BigDecimal costoActual = producto.getCostoPromedio() != null
                        ? java.math.BigDecimal.valueOf(producto.getCostoPromedio()) : cero;
                java.math.BigDecimal costoNumerador = costoActual.multiply(stockAnterior)
                        .add(precioCompraBd.multiply(dr.metros()));
                double nuevoCosto = costoNumerador.divide(stockNuevo, 6,
                        java.math.RoundingMode.HALF_UP).doubleValue();
                producto.setCostoPromedio(nuevoCosto);
            }
            productoRepository.save(producto);

            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(producto.getIdProducto(), sucursal.getIdSucursal())
                    .orElse(null);
            if (inv != null) {
                java.math.BigDecimal stockInv = inv.getStock() != null ? inv.getStock() : cero;
                inv.setStock(stockInv.add(dr.metros()).setScale(2, java.math.RoundingMode.HALF_UP));
                inventarioSucursalRepository.save(inv);
            }

            MovimientoStock ms = MovimientoStock.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .tipoMovimiento(TipoMovimiento.RECEPCION)
                    .cantidad(dr.metros())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(stockNuevo)
                    .referencia("Recepcion " + recepcion.getFolio())
                    .usuario(usuarioNombre)
                    .build();
            movimientoStockRepository.save(ms);
        }

        recepcion.setTotalMetros(totalMetros);
        recepcion.setTotalRollos(totalRollos);
        recepcion = recepcionRepository.save(recepcion);
        finanzasService.registrarCompra(recepcion.getIdRecepcion(), totalCompra, request.idTipoPago(),
                request.idSucursal(), recepcion.getFolio());

        auditoriaService.registrar("Recepcion", recepcion.getIdRecepcion(),
                "CREACION", persona.getUsuario(),
                "Recepcion " + recepcion.getFolio() + " - " + totalMetros + " m");

        return toResponse(recepcion, recepcionDetalleRepository.findByRecepcionIdRecepcion(recepcion.getIdRecepcion()));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Recepcion recepcion = buscarOExcepcion(id);
        Sucursal sucursal = recepcion.getSucursal();
        List<RecepcionDetalle> detalles = recepcionDetalleRepository.findByRecepcionIdRecepcion(id);

        for (RecepcionDetalle d : detalles) {
            Producto p = d.getProducto();
            java.math.BigDecimal stockP = p.getStockActual() != null
                    ? p.getStockActual() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal stockRestadoP = stockP.subtract(d.getMetros());
            p.setStockActual(stockRestadoP.compareTo(java.math.BigDecimal.ZERO) < 0
                    ? java.math.BigDecimal.ZERO : stockRestadoP.setScale(2, java.math.RoundingMode.HALF_UP));
            productoRepository.save(p);
            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(p.getIdProducto(), sucursal.getIdSucursal())
                    .orElse(null);
            if (inv != null) {
                java.math.BigDecimal stockInv = inv.getStock() != null
                        ? inv.getStock() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal stockRestadoInv = stockInv.subtract(d.getMetros());
                inv.setStock(stockRestadoInv.compareTo(java.math.BigDecimal.ZERO) < 0
                        ? java.math.BigDecimal.ZERO : stockRestadoInv.setScale(2, java.math.RoundingMode.HALF_UP));
                inventarioSucursalRepository.save(inv);
            }
        }

        recepcionDetalleRepository.deleteAll(detalles);
        recepcionRepository.delete(recepcion);

        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar("Recepcion", id, "ELIMINACION", usuario, "Recepcion " + recepcion.getFolio() + " eliminada");
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRecepciones", recepcionRepository.count());
        result.put("totalMetros", recepcionRepository.sumTotalMetros());
        result.put("totalRollos", recepcionRepository.sumTotalRollos());

        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        List<Recepcion> recientes = recepcionRepository.buscar(null, null).stream()
                .filter(r -> r.getFechaRecepcion() != null && !r.getFechaRecepcion().isBefore(desde))
                .toList();
        result.put("recepciones30Dias", recientes.size());
        result.put("metros30Dias", recientes.stream()
                .map(r -> r.getTotalMetros() != null ? r.getTotalMetros() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return result;
    }

    private Recepcion buscarOExcepcion(Integer id) {
        return recepcionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recepcion no encontrada con id: " + id));
    }

    private String generarFolio() {
        String prefix = "RE-";
        int num = 1;
        while (recepcionRepository.existsByFolio(prefix + String.format("%06d", num))) {
            num++;
        }
        return prefix + String.format("%06d", num);
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private RecepcionResponse toResponse(Recepcion r, List<RecepcionDetalle> detalles) {
        List<RecepcionDetalleResponse> detalleResponses = detalles.stream()
                .map(d -> new RecepcionDetalleResponse(
                        d.getIdRecepcionDetalle(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombre(),
                        d.getProducto().getSku(),
                        d.getMetros(), d.getRollos(),
                        d.getPrecioCompra(), d.getSubtotal()))
                .toList();

        return new RecepcionResponse(
                r.getIdRecepcion(), r.getFolio(),
                r.getProveedor() != null ? r.getProveedor().getIdProveedor() : null,
                r.getProveedor() != null ? r.getProveedor().getNombre() : null,
                r.getProveedor() != null ? r.getProveedor().getRfc() : null,
                r.getSucursal().getIdSucursal(), r.getSucursal().getNombre(),
                r.getUsuario() != null ? r.getUsuario().getIdPersona() : null,
                r.getUsuario() != null ? r.getUsuario().getUsuario() : null,
                r.getTotalMetros(), r.getTotalRollos(),
                r.getNota(), r.getFechaRecepcion(), detalleResponses);
    }
}
