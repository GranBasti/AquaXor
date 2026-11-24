-- Tabla para sincronizar las muestras de AquaXor en InsForge.
-- Ejecutar en el SQL editor del dashboard de InsForge (o con el MCP/CLI de InsForge).
CREATE TABLE IF NOT EXISTS muestras (
  id                  BIGSERIAL PRIMARY KEY,
  id_local            BIGINT           NOT NULL,   -- id de la muestra en Room (dispositivo)
  fecha_hora          BIGINT           NOT NULL,   -- epoch en milisegundos
  centro              TEXT             NOT NULL,
  tren                TEXT             NOT NULL,
  linea               TEXT             NOT NULL,
  tramo               TEXT             NOT NULL,
  profundidad_m       DOUBLE PRECISION,
  talla_mm            DOUBLE PRECISION,
  cantidad_individuos INTEGER          NOT NULL,
  estado              TEXT             NOT NULL,
  operador            TEXT             NOT NULL,
  observaciones       TEXT             NOT NULL DEFAULT '',
  creado_en           TIMESTAMPTZ      NOT NULL DEFAULT now()
);
