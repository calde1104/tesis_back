-- ============================================================
-- PLATAFORMA DE INTELIGENCIA PREDICTIVA B2B - V1 (schema base)
-- Reconstruido desde SHOW CREATE TABLE contra la base MySQL real
-- (servidor remoto, base `tesis`) el 2026-07-14. Este V1 nunca
-- existio como migracion Flyway: el schema ya estaba aplicado
-- directo en produccion/desarrollo. Se reconstruye aca para que
-- el repo sea reproducible desde cero.
-- ============================================================

USE tesis;

CREATE TABLE roles (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE departamentos (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE categorias (
    id INT NOT NULL AUTO_INCREMENT,
    id_departamento INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_categoria (id_departamento, nombre),
    CONSTRAINT fk_cat_depto FOREIGN KEY (id_departamento) REFERENCES departamentos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE empresa (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    nombre VARCHAR(255) NOT NULL,
    cuit VARCHAR(20) DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT '1',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY cuit (cuit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE usuarios (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    id_rol INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuario_email (id_empresa, email),
    KEY fk_usr_rol (id_rol),
    CONSTRAINT fk_usr_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id) ON DELETE RESTRICT,
    CONSTRAINT fk_usr_rol FOREIGN KEY (id_rol) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE marcas (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    KEY fk_marcas_empresa (id_empresa),
    CONSTRAINT fk_marcas_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE productos (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_marca VARCHAR(36) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    contenido_neto FLOAT NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_producto_sku (id_marca, sku),
    CONSTRAINT fk_prod_marca FOREIGN KEY (id_marca) REFERENCES marcas (id),
    CONSTRAINT productos_chk_1 CHECK (contenido_neto > 0),
    CONSTRAINT productos_chk_2 CHECK (unidad_medida IN ('ml','gr','kg','lt','un'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE productos_versiones (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_producto VARCHAR(36) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    contenido_neto FLOAT NOT NULL,
    unidad_medida VARCHAR(10) NOT NULL,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE DEFAULT NULL,
    PRIMARY KEY (id),
    KEY fk_pv_producto (id_producto),
    KEY fk_pv_categoria (id_categoria),
    CONSTRAINT fk_pv_categoria FOREIGN KEY (id_categoria) REFERENCES categorias (id),
    CONSTRAINT fk_pv_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT chk_pv_vigencia CHECK (vigencia_hasta IS NULL OR vigencia_hasta > vigencia_desde),
    CONSTRAINT productos_versiones_chk_1 CHECK (contenido_neto > 0),
    CONSTRAINT productos_versiones_chk_2 CHECK (unidad_medida IN ('ml','gr','kg','lt','un'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE config_sistema (
    id INT NOT NULL AUTO_INCREMENT,
    id_empresa VARCHAR(36) NOT NULL,
    id_producto VARCHAR(36) DEFAULT NULL,
    parametro VARCHAR(100) NOT NULL,
    valor TEXT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_config (id_empresa, parametro),
    KEY idx_cs_producto (id_producto),
    CONSTRAINT fk_cfg_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_cs_producto FOREIGN KEY (id_producto) REFERENCES productos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE archivos_ingesta (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    granularidad VARCHAR(10) NOT NULL,
    periodo_desde DATE NOT NULL,
    periodo_hasta DATE NOT NULL,
    fecha_recepcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY hash_sha256 (hash_sha256),
    KEY fk_ai_empresa (id_empresa),
    CONSTRAINT fk_ai_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT archivos_ingesta_chk_1 CHECK (estado IN ('PENDIENTE','PROCESANDO','OK','ERROR')),
    CONSTRAINT archivos_ingesta_chk_2 CHECK (granularidad IN ('SEMANAL','MENSUAL')),
    CONSTRAINT chk_ai_periodo CHECK (periodo_hasta >= periodo_desde)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE auditoria_integridad (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_archivo VARCHAR(36) NOT NULL,
    hash_sha256 CHAR(64) NOT NULL,
    resultado_validacion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_sello DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY id_archivo (id_archivo),
    CONSTRAINT fk_ai_archivo FOREIGN KEY (id_archivo) REFERENCES archivos_ingesta (id),
    CONSTRAINT auditoria_integridad_chk_1 CHECK (resultado_validacion IN ('PENDIENTE','VALIDO','INVALIDO','ALTERADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE eventos_sistema (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    id_producto VARCHAR(36) DEFAULT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    severidad VARCHAR(10) NOT NULL,
    payload JSON NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVA',
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME DEFAULT NULL,
    resuelto_por VARCHAR(36) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY fk_es_producto (id_producto),
    KEY fk_es_resuelto_por (resuelto_por),
    KEY idx_es_empresa (id_empresa),
    KEY idx_es_tipo (tipo_evento),
    KEY idx_es_severidad (severidad),
    KEY idx_es_estado (estado),
    CONSTRAINT fk_es_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_es_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT fk_es_resuelto_por FOREIGN KEY (resuelto_por) REFERENCES usuarios (id),
    CONSTRAINT eventos_sistema_chk_1 CHECK (severidad IN ('INFO','WARNING','CRITICO')),
    CONSTRAINT eventos_sistema_chk_2 CHECK (estado IN ('ACTIVA','VISTA','RESUELTA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE logs_acceso (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_usuario VARCHAR(36) NOT NULL,
    id_empresa VARCHAR(36) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    ip_origen VARCHAR(45) NOT NULL,
    resultado VARCHAR(10) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_la_usuario (id_usuario),
    KEY idx_la_empresa (id_empresa),
    KEY idx_la_fecha (fecha),
    CONSTRAINT fk_la_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_la_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id),
    CONSTRAINT logs_acceso_chk_1 CHECK (resultado IN ('OK','DENEGADO','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE modelos_versiones (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    version VARCHAR(20) NOT NULL,
    fecha_entrenamiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    periodo_datos_desde DATE NOT NULL,
    periodo_datos_hasta DATE NOT NULL,
    mae FLOAT NOT NULL,
    rmse FLOAT NOT NULL,
    mape FLOAT NOT NULL,
    hiperparametros JSON NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (id),
    UNIQUE KEY uq_modelo_version (id_empresa, version),
    CONSTRAINT fk_mv_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT modelos_versiones_chk_1 CHECK (mae >= 0),
    CONSTRAINT modelos_versiones_chk_2 CHECK (rmse >= 0),
    CONSTRAINT modelos_versiones_chk_3 CHECK (mape >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE predicciones (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    id_producto VARCHAR(36) NOT NULL,
    id_modelo VARCHAR(36) NOT NULL,
    anio INT NOT NULL,
    mes INT NOT NULL,
    unidades_predichas FLOAT NOT NULL,
    monto_predicho DECIMAL(14,2) NOT NULL,
    intervalo_confianza_inf FLOAT NOT NULL,
    intervalo_confianza_sup FLOAT NOT NULL,
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_prediccion (id_producto, id_modelo, anio, mes),
    KEY fk_pred_modelo (id_modelo),
    KEY idx_pred_empresa (id_empresa),
    KEY idx_pred_prod (id_producto),
    CONSTRAINT fk_pred_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_pred_modelo FOREIGN KEY (id_modelo) REFERENCES modelos_versiones (id),
    CONSTRAINT fk_pred_prod FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT chk_pred_intervalo CHECK (intervalo_confianza_sup >= intervalo_confianza_inf),
    CONSTRAINT predicciones_chk_1 CHECK (anio BETWEEN 2000 AND 2100),
    CONSTRAINT predicciones_chk_2 CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT predicciones_chk_3 CHECK (unidades_predichas >= 0),
    CONSTRAINT predicciones_chk_4 CHECK (monto_predicho >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE simulaciones (
    id VARCHAR(36) NOT NULL DEFAULT (uuid()),
    id_empresa VARCHAR(36) NOT NULL,
    id_usuario VARCHAR(36) NOT NULL,
    id_modelo VARCHAR(36) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    parametros_entrada JSON NOT NULL,
    resultado JSON NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY fk_sim_empresa (id_empresa),
    KEY fk_sim_usuario (id_usuario),
    KEY fk_sim_modelo (id_modelo),
    CONSTRAINT fk_sim_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_sim_modelo FOREIGN KEY (id_modelo) REFERENCES modelos_versiones (id),
    CONSTRAINT fk_sim_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ventas_mensuales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_producto VARCHAR(36) NOT NULL,
    id_archivo VARCHAR(36) NOT NULL,
    id_empresa VARCHAR(36) NOT NULL,
    anio INT NOT NULL,
    mes INT NOT NULL,
    unidades_vendidas INT NOT NULL,
    monto_facturado DECIMAL(14,2) NOT NULL,
    es_agregado TINYINT(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (id),
    UNIQUE KEY uq_venta_mensual (id_producto, id_empresa, anio, mes),
    KEY fk_vm_archivo (id_archivo),
    KEY idx_vm_empresa (id_empresa),
    KEY idx_vm_prod (id_producto),
    KEY idx_vm_periodo (anio, mes),
    CONSTRAINT fk_vm_archivo FOREIGN KEY (id_archivo) REFERENCES archivos_ingesta (id),
    CONSTRAINT fk_vm_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_vm_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT ventas_mensuales_chk_1 CHECK (anio BETWEEN 2000 AND 2100),
    CONSTRAINT ventas_mensuales_chk_2 CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ventas_mensuales_chk_3 CHECK (unidades_vendidas >= 0),
    CONSTRAINT ventas_mensuales_chk_4 CHECK (monto_facturado >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ventas_semanales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_producto VARCHAR(36) NOT NULL,
    id_archivo VARCHAR(36) NOT NULL,
    id_empresa VARCHAR(36) NOT NULL,
    anio INT NOT NULL,
    semana INT NOT NULL,
    unidades_vendidas INT NOT NULL,
    monto_facturado DECIMAL(14,2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_venta_semanal (id_producto, id_archivo, anio, semana),
    KEY fk_vs_archivo (id_archivo),
    KEY idx_vs_empresa (id_empresa),
    KEY idx_vs_prod (id_producto),
    KEY idx_vs_periodo (anio, semana),
    CONSTRAINT fk_vs_archivo FOREIGN KEY (id_archivo) REFERENCES archivos_ingesta (id),
    CONSTRAINT fk_vs_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT fk_vs_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT ventas_semanales_chk_1 CHECK (anio BETWEEN 2000 AND 2100),
    CONSTRAINT ventas_semanales_chk_2 CHECK (semana BETWEEN 1 AND 53),
    CONSTRAINT ventas_semanales_chk_3 CHECK (unidades_vendidas >= 0),
    CONSTRAINT ventas_semanales_chk_4 CHECK (monto_facturado >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
