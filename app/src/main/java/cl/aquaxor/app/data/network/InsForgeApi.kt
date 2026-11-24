package cl.aquaxor.app.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Cliente REST de InsForge (backend tipo PostgREST).
 * Inserta con POST /api/database/records/{tabla} enviando un ARREGLO JSON.
 * La autenticación (Bearer) la agrega un interceptor de OkHttp.
 */
interface InsForgeApi {
    @POST("api/database/records/{tabla}")
    suspend fun insertar(
        @Path("tabla") tabla: String,
        @Body filas: List<MuestraRemota>,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<MuestraRemota>>

    @GET("api/database/records/{tabla}")
    suspend fun listar(
        @Path("tabla") tabla: String,
        @Query("order") orden: String = "fecha_hora.desc",
        @Query("limit") limite: Int = 50
    ): Response<List<MuestraRemota>>

    companion object {
        const val TABLA_MUESTRAS = "muestras"
    }
}

/** Fila de la tabla `muestras` en InsForge (ver docs/insforge_schema.sql). */
data class MuestraRemota(
    @SerializedName("id_local") val idLocal: Long,
    @SerializedName("fecha_hora") val fechaHora: Long,
    val centro: String,
    val tren: String,
    val linea: String,
    val tramo: String,
    @SerializedName("profundidad_m") val profundidadM: Double?,
    @SerializedName("talla_mm") val tallaMm: Double?,
    @SerializedName("cantidad_individuos") val cantidadIndividuos: Int,
    val estado: String,
    val operador: String,
    val observaciones: String
)
