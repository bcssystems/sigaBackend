package com.bcsystems.siga.repository;

import com.bcsystems.siga.domain.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {
    List<VentaDetalle> findByVentaIdVenta(Integer idVenta);

    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd " +
           "WHERE vd.producto.idProducto = :idProducto " +
           "AND vd.venta.caja.sucursal.idSucursal = :idSucursal " +
           "AND vd.venta.estado = com.bcsystems.siga.domain.en.EstadoVenta.ESPERA")
    java.math.BigDecimal sumCantidadEnEspera(@Param("idProducto") Integer idProducto,
                                @Param("idSucursal") Integer idSucursal);
}
