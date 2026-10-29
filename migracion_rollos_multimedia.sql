-- Migración SIGA: eliminación de rollos y multimedia por producto.
-- Respaldar la base de datos antes de ejecutar. Revisar primero con SHOW COLUMNS.

ALTER TABLE producto DROP COLUMN metros_por_rollo;
ALTER TABLE recepcion DROP COLUMN total_rollos;
ALTER TABLE recepcion_detalle DROP COLUMN rollos;

-- Columnas históricas de Envers relacionadas con los campos eliminados.
ALTER TABLE producto_aud DROP COLUMN metros_por_rollo;
ALTER TABLE recepcion_aud DROP COLUMN total_rollos;
ALTER TABLE recepcion_detalle_aud DROP COLUMN rollos;

CREATE TABLE producto_multimedia (
    id_multimedia INT NOT NULL AUTO_INCREMENT,
    id_producto INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    url VARCHAR(500) NOT NULL,
    nombre_archivo VARCHAR(255),
    es_principal BIT NOT NULL DEFAULT 0,
    orden INT NOT NULL DEFAULT 0,
    fecha_subida DATETIME(6) NOT NULL,
    PRIMARY KEY (id_multimedia),
    CONSTRAINT fk_producto_multimedia_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    INDEX idx_producto_multimedia_producto_orden (id_producto, orden)
);

CREATE TABLE producto_multimedia_aud (
    id_multimedia INT NOT NULL,
    REV INT NOT NULL,
    REVTYPE TINYINT,
    id_producto INT,
    tipo VARCHAR(20),
    url VARCHAR(500),
    nombre_archivo VARCHAR(255),
    es_principal BIT,
    orden INT,
    fecha_subida DATETIME(6),
    PRIMARY KEY (id_multimedia, REV),
    CONSTRAINT fk_producto_multimedia_aud_rev FOREIGN KEY (REV) REFERENCES revinfo (REV)
);
