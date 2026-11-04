package cl.aquaxor.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.aquaxor.app.model.ConteoEstado
import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.MuestraResumen
import kotlinx.coroutines.flow.Flow

private const val SELECT_RESUMEN = """
SELECT m.*,
       c.nombre AS centroNombre,
       t.nombre AS trenNombre,
       l.nombre AS lineaNombre,
       u.nombre AS operadorNombre,
       (SELECT COUNT(*) FROM adjuntos a WHERE a.muestraId = m.id AND a.tipo = 'FOTO') AS totalFotos,
       (SELECT COUNT(*) FROM adjuntos a WHERE a.muestraId = m.id AND a.tipo = 'DOCUMENTO') AS totalDocumentos
FROM muestras m
JOIN centros c ON c.id = m.centroId
JOIN trenes t ON t.id = m.trenId
JOIN lineas l ON l.id = m.lineaId
JOIN usuarios u ON u.id = m.usuarioId
"""

@Dao
interface MuestraDao {

    /** Búsqueda de texto + filtros (estado, fecha desde, centro). */
    @Query(
        SELECT_RESUMEN + """
WHERE (:estado IS NULL OR m.estado = :estado)
  AND m.fechaHora >= :desde
  AND (:centroId IS NULL OR m.centroId = :centroId)
  AND (:texto = ''
       OR l.nombre LIKE '%' || :texto || '%'
       OR t.nombre LIKE '%' || :texto || '%'
       OR c.nombre LIKE '%' || :texto || '%'
       OR u.nombre LIKE '%' || :texto || '%'
       OR m.tramo LIKE '%' || :texto || '%'
       OR m.observaciones LIKE '%' || :texto || '%'
       OR CAST(m.id AS TEXT) LIKE '%' || :texto || '%')
ORDER BY m.fechaHora DESC
"""
    )
    fun buscar(texto: String, estado: String?, desde: Long, centroId: Long?): Flow<List<MuestraResumen>>

    @Query(SELECT_RESUMEN + " WHERE m.id = :id")
    fun observarResumen(id: Long): Flow<MuestraResumen?>

    @Query("SELECT * FROM muestras WHERE id = :id")
    suspend fun obtener(id: Long): Muestra?

    @Insert
    suspend fun insertar(muestra: Muestra): Long

    @Update
    suspend fun actualizar(muestra: Muestra)

    @Query("DELETE FROM muestras WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("UPDATE muestras SET estado = :estado, sincronizada = 0 WHERE id = :id")
    suspend fun cambiarEstado(id: Long, estado: String)

    @Query("UPDATE muestras SET sincronizada = 1 WHERE sincronizada = 0")
    suspend fun marcarSincronizadas(): Int

    // ---- Indicadores ----
    @Query("SELECT COUNT(*) FROM muestras")
    fun totalMuestras(): Flow<Int>

    @Query("SELECT estado AS estado, COUNT(*) AS total FROM muestras GROUP BY estado")
    fun conteoPorEstado(): Flow<List<ConteoEstado>>

    @Query("SELECT COALESCE(SUM(cantidadIndividuos), 0) FROM muestras WHERE fechaHora >= :desde")
    fun individuosDesde(desde: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM muestras WHERE sincronizada = 0")
    fun sinSincronizar(): Flow<Int>
}
