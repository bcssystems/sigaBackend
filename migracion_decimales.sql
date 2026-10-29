-- Migracion cantidades/stock a DECIMAL(19,2)
-- 1) Respaldo de la BD antes de ejecutar.
-- 2) Verificar nombres de columnas: SHOW COLUMNS FROM <tabla>;
-- 3) Ejecutar y luego poner spring.jpa.hibernate.ddl-auto=validate

ALTER TABLE producto MODIFY stock_actual DECIMAL(19,2) NOT NULL;
ALTER TABLE producto MODIFY stock_minimo DECIMAL(19,2);
ALTER TABLE producto MODIFY stock_maximo DECIMAL(19,2);

ALTER TABLE producto_aud MODIFY stock_actual DECIMAL(19,2) NOT NULL;
ALTER TABLE producto_aud MODIFY stock_minimo DECIMAL(19,2);
ALTER TABLE producto_aud MODIFY stock_maximo DECIMAL(19,2);

ALTER TABLE inventario_sucursal MODIFY stock DECIMAL(19,2) NOT NULL;
ALTER TABLE inventario_sucursal MODIFY stock_minimo DECIMAL(19,2);
ALTER TABLE inventario_sucursal MODIFY stock_maximo DECIMAL(19,2);

ALTER TABLE inventario_sucursal_aud MODIFY stock DECIMAL(19,2) NOT NULL;
ALTER TABLE inventario_sucursal_aud MODIFY stock_minimo DECIMAL(19,2);
ALTER TABLE inventario_sucursal_aud MODIFY stock_maximo DECIMAL(19,2);

ALTER TABLE movimiento_stock MODIFY cantidad DECIMAL(19,2) NOT NULL;
ALTER TABLE movimiento_stock MODIFY stock_anterior DECIMAL(19,2) NOT NULL;
ALTER TABLE movimiento_stock MODIFY stock_nuevo DECIMAL(19,2) NOT NULL;

ALTER TABLE movimiento_stock_aud MODIFY cantidad DECIMAL(19,2) NOT NULL;
ALTER TABLE movimiento_stock_aud MODIFY stock_anterior DECIMAL(19,2) NOT NULL;
ALTER TABLE movimiento_stock_aud MODIFY stock_nuevo DECIMAL(19,2) NOT NULL;

ALTER TABLE reserva_producto MODIFY cantidad DECIMAL(19,2) NOT NULL;

ALTER TABLE carrito_item_rapido MODIFY cantidad DECIMAL(19,2) NOT NULL;

ALTER TABLE cotizacion_detalle MODIFY cantidad DECIMAL(19,2) NOT NULL;
ALTER TABLE cotizacion_detalle_aud MODIFY cantidad DECIMAL(19,2) NOT NULL;

ALTER TABLE venta_detalle MODIFY cantidad DECIMAL(19,2) NOT NULL;
ALTER TABLE venta_detalle_aud MODIFY cantidad DECIMAL(19,2) NOT NULL;

ALTER TABLE recepcion MODIFY total_metros DECIMAL(19,2) NOT NULL;
ALTER TABLE recepcion_aud MODIFY total_metros DECIMAL(19,2) NOT NULL;

ALTER TABLE recepcion_detalle MODIFY metros DECIMAL(19,2) NOT NULL;
ALTER TABLE recepcion_detalle_aud MODIFY metros DECIMAL(19,2) NOT NULL;

ALTER TABLE auditoria MODIFY cantidad DECIMAL(19,2);
ALTER TABLE auditoria MODIFY stock_anterior DECIMAL(19,2);
ALTER TABLE auditoria MODIFY stock_nuevo DECIMAL(19,2);

ALTER TABLE auditoria_aud MODIFY cantidad DECIMAL(19,2);
ALTER TABLE auditoria_aud MODIFY stock_anterior DECIMAL(19,2);
ALTER TABLE auditoria_aud MODIFY stock_nuevo DECIMAL(19,2);
