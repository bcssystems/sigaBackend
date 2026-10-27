package com.bcsystems.siga.controller;

import com.bcsystems.siga.dto.PrecioClienteRequest;
import com.bcsystems.siga.dto.PrecioClienteResponse;
import com.bcsystems.siga.service.PrecioClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes/{idCliente}/precios")
public class PrecioClienteController {

    private final PrecioClienteService precioClienteService;

    public PrecioClienteController(PrecioClienteService precioClienteService) {
        this.precioClienteService = precioClienteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_VER')")
    public ResponseEntity<List<PrecioClienteResponse>> listar(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(precioClienteService.listarPorCliente(idCliente));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_EDITAR')")
    public ResponseEntity<List<PrecioClienteResponse>> guardar(
            @PathVariable Integer idCliente,
            @Valid @RequestBody List<PrecioClienteRequest> precios) {
        return ResponseEntity.ok(precioClienteService.guardarPreciosCliente(idCliente, precios));
    }

    @DeleteMapping("/{idPrecioCliente}")
    @PreAuthorize("hasAuthority('PRECIOS_CLIENTE_EDITAR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idPrecioCliente) {
        precioClienteService.eliminarPrecio(idPrecioCliente);
        return ResponseEntity.noContent().build();
    }
}