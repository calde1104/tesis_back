-- ============================================================
-- V8 - Habilita la baja logica (soft-delete) de cuentas de usuario
-- (RF13). Mismo patron que archivos_ingesta.estado = 'ELIMINADO'
-- (V5): no se borra fisicamente la fila para preservar la
-- trazabilidad de auditoria (logs_acceso, simulaciones,
-- eventos_sistema.resuelto_por referencian al usuario por FK
-- RESTRICT); un hard-delete real rompe esas referencias.
-- ============================================================

USE tesis;

ALTER TABLE usuarios
    ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;
