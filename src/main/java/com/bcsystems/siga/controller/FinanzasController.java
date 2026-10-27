package com.bcsystems.siga.controller;

import com.bcsystems.siga.dto.*;
import com.bcsystems.siga.service.FinanzasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finanzas")
public class FinanzasController {
    private final FinanzasService service;
    public FinanzasController(FinanzasService service) { this.service = service; }

    @GetMapping("/cuentas")
    @PreAuthorize("hasAuthority('FINANZAS_VER')")
    public ResponseEntity<List<CuentaFinancieraResponse>> cuentas() { return ResponseEntity.ok(service.listarCuentas()); }

    @PostMapping("/cuentas")
    @PreAuthorize("hasAuthority('CUENTAS_FINANCIERAS_EDITAR')")
    public ResponseEntity<CuentaFinancieraResponse> crearCuenta(@RequestBody CuentaFinancieraRequest request) { return ResponseEntity.ok(service.crearCuenta(request)); }

    @PutMapping("/cuentas/{id}")
    @PreAuthorize("hasAuthority('CUENTAS_FINANCIERAS_EDITAR')")
    public ResponseEntity<CuentaFinancieraResponse> actualizarCuenta(@PathVariable Integer id, @RequestBody CuentaFinancieraRequest request) { return ResponseEntity.ok(service.actualizarCuenta(id, request)); }

    @DeleteMapping("/cuentas/{id}")
    @PreAuthorize("hasAuthority('CUENTAS_FINANCIERAS_EDITAR')")
    public ResponseEntity<Void> desactivarCuenta(@PathVariable Integer id) { service.desactivarCuenta(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('FINANZAS_VER')")
    public ResponseEntity<FinanzasResumenResponse> resumen(@RequestParam(required = false) Integer idCuenta,
            @RequestParam(required = false) String tipo, @RequestParam(required = false) String categoria,
            @RequestParam(required = false) LocalDateTime inicio, @RequestParam(required = false) LocalDateTime fin) {
        return ResponseEntity.ok(service.resumen(idCuenta, tipo, categoria, inicio, fin));
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAuthority('FINANZAS_VER')")
    public ResponseEntity<Page<MovimientoFinancieroResponse>> movimientos(@RequestParam(required = false) Integer idCuenta,
            @RequestParam(required = false) String tipo, @RequestParam(required = false) String categoria,
            @RequestParam(required = false) LocalDateTime inicio, @RequestParam(required = false) LocalDateTime fin,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(service.movimientos(idCuenta, tipo, categoria, inicio, fin, pageable));
    }
}
