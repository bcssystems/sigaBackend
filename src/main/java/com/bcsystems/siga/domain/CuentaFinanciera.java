package com.bcsystems.siga.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
@Table(name = "cuenta_financiera")
public class CuentaFinanciera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCuenta;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    @Builder.Default
    private Double saldo = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
