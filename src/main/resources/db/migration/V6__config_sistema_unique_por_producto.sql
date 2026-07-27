ALTER TABLE config_sistema
    DROP INDEX uq_config,
    ADD UNIQUE KEY uq_config_producto (id_empresa, id_producto, parametro);
