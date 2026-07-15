-- ============================================================
-- PLATAFORMA DE INTELIGENCIA PREDICTIVA B2B - V4
-- Features calculadas de ventas: lags, media movil, market share, price gap
-- ============================================================
--
-- Estas columnas del Excel (lag_1, lag_2, lag_12, ma_3m,
-- market_share_unidades, price_gap_pct) tienen la MISMA granularidad
-- que ventas_mensuales (1 fila por producto + periodo), asi que se
-- agregan ahi en vez de crear una tabla nueva.
--
-- Son snapshots ya calculados en el pipeline de enriquecimiento del
-- Excel (no se recalculan en el backend). Si mas adelante el backend
-- termina calculando market_share_unidades / price_gap_pct on-the-fly
-- (via /v1/market-share/overview o /price-gap), estas columnas quedan
-- como el valor de referencia del momento de la carga / feature de
-- entrenamiento del modelo, no como fuente de verdad en vivo.
-- ============================================================

USE tesis;

ALTER TABLE ventas_mensuales
    ADD COLUMN lag_1                 INT   NULL AFTER precio_unitario,
    ADD COLUMN lag_2                 INT   NULL AFTER lag_1,
    ADD COLUMN lag_12                INT   NULL AFTER lag_2,
    ADD COLUMN ma_3m                 FLOAT NULL AFTER lag_12,
    ADD COLUMN market_share_unidades FLOAT NULL AFTER ma_3m,
    ADD COLUMN price_gap_pct         FLOAT NULL AFTER market_share_unidades;

-- ============================================================
-- FIN MIGRACION V4
-- ============================================================
