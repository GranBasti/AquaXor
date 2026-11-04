package cl.aquaxor.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.aquaxor.app.model.Adjunto
import kotlinx.coroutines.flow.Flow

@Dao
interface AdjuntoDao {
    @Query("SELECT * FROM adjuntos WHERE muestraId = :muestraId ORDER BY creadoEn")
    fun observarDe(muestraId: Long): Flow<List<Adjunto>>

    @Query("SELECT * FROM adjuntos WHERE muestraId = :muestraId ORDER BY creadoEn")
    suspend fun listaDe(muestraId: Long): List<Adjunto>

    @Insert
    suspend fun insertar(adjuntos: List<Adjunto>)

    @Query("DELETE FROM adjuntos WHERE id IN (:ids)")
    suspend fun eliminar(ids: List<Long>)
}
