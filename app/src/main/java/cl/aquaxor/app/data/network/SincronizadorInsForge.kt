package cl.aquaxor.app.data.network

import cl.aquaxor.app.model.MuestraResumen

/** Lo mínimo que el sincronizador necesita de la base local (la implementa AquaRepository). */
interface PendientesStore {
    suspend fun pendientesDeSincronizar(): List<MuestraResumen>
    suspend fun marcarSincronizadas(ids: List<Long>): Int
}

sealed interface ResultadoSync {
    data object NoConfigurado : ResultadoSync
    data object SinPendientes : ResultadoSync
    data class Exito(val cantidad: Int) : ResultadoSync
    data class Error(val mensaje: String) : ResultadoSync
}

fun MuestraResumen.aRemota() = MuestraRemota(
    idLocal = muestra.id,
    fechaHora = muestra.fechaHora,
    centro = centroNombre,
    tren = trenNombre,
    linea = lineaNombre,
    tramo = muestra.tramo,
    profundidadM = muestra.profundidadM,
    tallaMm = muestra.tallaMm,
    cantidadIndividuos = muestra.cantidadIndividuos,
    estado = muestra.estado.name,
    operador = operadorNombre,
    observaciones = muestra.observaciones
)

/**
 * Envía a InsForge las muestras pendientes y, solo si el servidor responde 2xx,
 * las marca como sincronizadas en Room. `api` es null cuando falta configuración.
 */
class SincronizadorInsForge(
    private val api: InsForgeApi?,
    private val store: PendientesStore
) {
    suspend fun sincronizar(): ResultadoSync {
        val servicio = api ?: return ResultadoSync.NoConfigurado
        val pendientes = store.pendientesDeSincronizar()
        if (pendientes.isEmpty()) return ResultadoSync.SinPendientes
        return try {
            val resp = servicio.insertar(InsForgeApi.TABLA_MUESTRAS, pendientes.map { it.aRemota() })
            if (resp.isSuccessful) {
                store.marcarSincronizadas(pendientes.map { it.muestra.id })
                ResultadoSync.Exito(pendientes.size)
            } else {
                ResultadoSync.Error("El servidor respondió ${resp.code()}")
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoSync.Error("Falla de red: ${e.message ?: "sin detalle"}")
        }
    }
}
