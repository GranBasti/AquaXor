package cl.aquaxor.app.data.network

/** Ubicación de trabajo por defecto: Puerto Montt (zona mitilicultora de Los Lagos). */
object Ubicacion {
    const val NOMBRE = "Puerto Montt"
    const val LAT = -41.4717
    const val LON = -72.9369
}

sealed interface ResultadoClima {
    data class Exito(val clima: Clima) : ResultadoClima
    data class Error(val mensaje: String) : ResultadoClima
}

class WeatherRepository(private val api: WeatherApi) {
    suspend fun obtener(lat: Double = Ubicacion.LAT, lon: Double = Ubicacion.LON): ResultadoClima =
        try {
            val clima = api.pronostico(lat, lon).aClima()
            if (clima == null) ResultadoClima.Error("La API no entregó datos actuales")
            else ResultadoClima.Exito(clima)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultadoClima.Error("No se pudo consultar el clima: ${e.message ?: "error de red"}")
        }
}
