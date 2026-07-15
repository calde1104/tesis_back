-- ============================================================
-- PLATAFORMA DE INTELIGENCIA PREDICTIVA B2B - V3
-- Variables de contexto (clima / calendario / macro) + precio unitario
-- ============================================================
--
-- Se revisó dataset_enriquecido_v3.xlsx: las columnas de clima,
-- feriados/calendario y macroeconomía son iguales para todas las filas
-- de un mismo (anio, mes) sin importar empresa, marca o SKU (se validó
-- sobre 113.949 filas de la hoja 2021 sin ninguna inconsistencia). Por
-- eso las 3 tablas nuevas son de referencia global, con clave única
-- (anio, mes) y SIN id_empresa: no son datos por tenant, son contexto
-- de mercado/país que consultan todas las organizaciones por igual.
--
-- Esto además calza con el módulo de backend ya definido
-- (ContextController / ContextService = macro + clima + calendario) y
-- con los endpoints GET /v1/context/macro, /clima y /calendario.
--
-- precio_unitario en el Excel varía mes a mes (es monto_facturado /
-- unidades_vendidas), no es un atributo fijo del producto. Por eso:
--   - productos.valor_unitario  -> ÚLTIMO precio conocido del SKU
--     (se recalcula en cada import; sirve para listados/tarjetas de
--     producto donde alcanza con "el precio actual").
--   - ventas_mensuales.precio_unitario -> precio de ESE mes puntual,
--     que es lo que en realidad necesitan los endpoints de evolución
--     de precio y price-gap (/v1/market-share/price-gap).
-- Si más adelante el price-gap y compañía terminan usando solo
-- ventas_mensuales.precio_unitario, la columna en productos puede
-- quedar como campo derivado/cache nomás.
-- ============================================================

USE tesis;

-- ------------------------------------------------------------
-- 1) Precio / valor unitario
-- ------------------------------------------------------------

ALTER TABLE productos
    ADD COLUMN valor_unitario DECIMAL(14,2) NULL AFTER unidad_medida;

ALTER TABLE ventas_mensuales
    ADD COLUMN precio_unitario DECIMAL(14,2) NULL AFTER monto_facturado;

-- ------------------------------------------------------------
-- 2) Variables climáticas -> GET /v1/context/clima
-- ------------------------------------------------------------

CREATE TABLE variables_climaticas (
    id                   INT             NOT NULL AUTO_INCREMENT PRIMARY KEY,
    anio                 INT             NOT NULL CHECK (anio BETWEEN 2000 AND 2100),
    mes                  INT             NOT NULL CHECK (mes BETWEEN 1 AND 12),
    temp_max_promedio    FLOAT           NULL,
    temp_media_promedio  FLOAT           NULL,
    precipitacion_mm     FLOAT           NULL,
    UNIQUE KEY uq_clima_periodo (anio, mes)
);

-- ------------------------------------------------------------
-- 3) Variables de calendario: feriados + eventos comerciales +
--    fechas de pago especiales (aguinaldo) -> GET /v1/context/calendario
-- ------------------------------------------------------------

CREATE TABLE variables_calendario (
    id                       INT             NOT NULL AUTO_INCREMENT PRIMARY KEY,
    anio                     INT             NOT NULL CHECK (anio BETWEEN 2000 AND 2100),
    mes                      INT             NOT NULL CHECK (mes BETWEEN 1 AND 12),
    cantidad_feriados        INT             NOT NULL DEFAULT 0 CHECK (cantidad_feriados >= 0),
    tiene_festivo_comercial  BOOLEAN         NOT NULL DEFAULT FALSE,
    nombre_festivo           VARCHAR(255)    NULL,
    quincena_aguinaldo       BOOLEAN         NOT NULL DEFAULT FALSE,
    inicio_ciclo_escolar     BOOLEAN         NOT NULL DEFAULT FALSE,
    ciclo_cobro_mensual      BOOLEAN         NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_calendario_periodo (anio, mes)
);

-- ------------------------------------------------------------
-- 4) Variables macroeconómicas -> GET /v1/context/macro
-- ------------------------------------------------------------

CREATE TABLE variables_macroeconomicas (
    id                   INT             NOT NULL AUTO_INCREMENT PRIMARY KEY,
    anio                 INT             NOT NULL CHECK (anio BETWEEN 2000 AND 2100),
    mes                  INT             NOT NULL CHECK (mes BETWEEN 1 AND 12),
    ipc_indice           FLOAT           NULL,
    ipc_var_interanual   FLOAT           NULL,
    tipo_cambio_pyg_usd  FLOAT           NULL,
    pbi_crecimiento      FLOAT           NULL,
    tasa_desempleo       FLOAT           NULL,
    remesas_usd          DECIMAL(18,2)   NULL,
    inversion_ext_usd    DECIMAL(18,2)   NULL,
    UNIQUE KEY uq_macro_periodo (anio, mes)
);

-- ============================================================
-- FIN MIGRACION V3
-- ============================================================

-- Nota: en el Excel también vienen lag_1, lag_2, lag_12, ma_3m,
-- market_share_unidades y price_gap_pct. No se crearon tablas para
-- esas porque son features calculadas (lags/medias móviles del propio
-- historial de ventas, y salidas de los endpoints de forecast /
-- market-share), no "variables de contexto" nuevas. Si las querés
-- persistir igual avisame y vemos dónde encajan.
