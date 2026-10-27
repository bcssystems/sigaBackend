package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "movimiento_financiero", indexes = {
        @Index(name = "idx_fin_fecha", columnList = "fecha"),
        @Index(name = "idx_fin_origen", columnList = "origen,id_origen")
})
public class MovimientoFinanciero {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaFinanciera cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_pago")
    private TipoPago tipoPago;

    @Column(nullable = false, length = 15)
    private String tipo;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false, length = 40)
    private String origen;

    @Column(name = "id_origen")
    private Integer idOrigen;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Persona usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
