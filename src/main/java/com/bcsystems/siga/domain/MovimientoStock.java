package com.bcsystems.siga.domain;

import com.bcsystems.siga.domain.en.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movimiento_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal cantidad;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal stockAnterior;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal stockNuevo;

    @Column(length = 50)
    private String referencia;

    @Column(length = 100)
    private String usuario;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;
}
