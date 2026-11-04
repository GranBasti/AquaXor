package cl.aquaxor.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Tren
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {
    // Centros
    @Query("SELECT * FROM centros ORDER BY nombre")
    fun centros(): Flow<List<Centro>>

    @Insert
    suspend fun insertarCentro(centro: Centro): Long

    @Update
    suspend fun actualizarCentro(centro: Centro)

    @Delete
    suspend fun eliminarCentro(centro: Centro)

    // Trenes
    @Query("SELECT * FROM trenes ORDER BY nombre")
    fun trenes(): Flow<List<Tren>>

    @Query("SELECT * FROM trenes WHERE centroId = :centroId ORDER BY nombre")
    fun trenesDe(centroId: Long): Flow<List<Tren>>

    @Insert
    suspend fun insertarTren(tren: Tren): Long

    @Update
    suspend fun actualizarTren(tren: Tren)

    @Delete
    suspend fun eliminarTren(tren: Tren)

    // Líneas
    @Query("SELECT * FROM lineas ORDER BY nombre")
    fun lineas(): Flow<List<Linea>>

    @Query("SELECT * FROM lineas WHERE trenId = :trenId ORDER BY nombre")
    fun lineasDe(trenId: Long): Flow<List<Linea>>

    @Insert
    suspend fun insertarLinea(linea: Linea): Long

    @Update
    suspend fun actualizarLinea(linea: Linea)

    @Delete
    suspend fun eliminarLinea(linea: Linea)
}
