package com.bcsystems.bonds.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventario_sucursal", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_producto", "id_sucursal"})
})
public class InventarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal stock;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal stockMinimo;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal stockMaximo;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}
