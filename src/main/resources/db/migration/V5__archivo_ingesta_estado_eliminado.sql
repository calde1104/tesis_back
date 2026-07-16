-- ============================================================
-- V5 - Habilita el estado ELIMINADO (soft-delete) en archivos_ingesta
-- Decision confirmada con Edga: DELETE /v1/{empresaId}/ingestion/{archivoId}/borrado
-- es soft-delete (no borra fisicamente ventas_mensuales/ventas_semanales
-- asociadas). Ver tesis_back/bitacora.md seccion 4.
-- ============================================================

USE tesis;

ALTER TABLE archivos_ingesta
    DROP CHECK archivos_ingesta_chk_1;

ALTER TABLE archivos_ingesta
    ADD CONSTRAINT archivos_ingesta_chk_1
        CHECK (estado IN ('PENDIENTE','PROCESANDO','OK','ERROR','ELIMINADO'));
