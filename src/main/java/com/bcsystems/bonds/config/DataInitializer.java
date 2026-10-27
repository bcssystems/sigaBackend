package com.bcsystems.bonds.config;

import com.bcsystems.bonds.domain.TipoPago;
import com.bcsystems.bonds.domain.CuentaFinanciera;
import com.bcsystems.bonds.repository.TipoPagoRepository;
import com.bcsystems.bonds.repository.CuentaFinancieraRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TipoPagoRepository tipoPagoRepository;
    private final CuentaFinancieraRepository cuentaFinancieraRepository;

    public DataInitializer(TipoPagoRepository tipoPagoRepository, CuentaFinancieraRepository cuentaFinancieraRepository) {
        this.tipoPagoRepository = tipoPagoRepository;
        this.cuentaFinancieraRepository = cuentaFinancieraRepository;
    }

    @Override
    public void run(String... args) {
        if (tipoPagoRepository.count() == 0) {
            String[] formas = {
                "EFECTIVO", "TARJETA DE CREDITO", "TARJETA DE DEBITO",
                "TRANSFERENCIA", "DEPOSITO", "CHEQUE",
                "MONEDERO ELECTRONICO", "QR", "OTRO"
            };
            for (String nombre : formas) {
                tipoPagoRepository.save(TipoPago.builder()
                        .nombre(nombre)
                        .activo(true)
                        .build());
            }
        }
        CuentaFinanciera efectivo = cuentaFinancieraRepository.findAll().stream()
                .filter(c -> "Caja de efectivo".equalsIgnoreCase(c.getNombre())).findFirst()
                .orElseGet(() -> cuentaFinancieraRepository.save(CuentaFinanciera.builder()
                        .nombre("Caja de efectivo").tipo("EFECTIVO").saldo(0.0).activa(true).build()));
        CuentaFinanciera banco = cuentaFinancieraRepository.findAll().stream()
                .filter(c -> "Banco principal".equalsIgnoreCase(c.getNombre())).findFirst()
                .orElseGet(() -> cuentaFinancieraRepository.save(CuentaFinanciera.builder()
                        .nombre("Banco principal").tipo("BANCO").saldo(0.0).activa(true).build()));
        tipoPagoRepository.findAll().forEach(tp -> {
            if (tp.getCuentaFinanciera() != null) return;
            String n = tp.getNombre().toUpperCase();
            tp.setCuentaFinanciera(n.contains("EFECT") ? efectivo : banco);
            tipoPagoRepository.save(tp);
        });
    }
}
