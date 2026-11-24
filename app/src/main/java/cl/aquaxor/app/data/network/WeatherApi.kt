package cl.aquaxor.app.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/** Interfaz Retrofit para Open-Meteo (https://open-meteo.com), API de clima gratuita y sin clave. */
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun pronostico(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = CAMPOS_ACTUALES,
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("forecast_days") dias: Int = 1,
        @Query("timezone") zona: String = "auto"
    ): ForecastDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val CAMPOS_ACTUALES =
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m"
    }
}

data class ForecastDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val current: CurrentDto? = null,
    val hourly: HourlyDto? = null
)

data class CurrentDto(
    val time: String? = null,
    @SerializedName("temperature_2m") val temperatura: Double? = null,
    @SerializedName("relative_humidity_2m") val humedad: Int? = null,
    @SerializedName("apparent_temperature") val sensacion: Double? = null,
    val precipitation: Double? = null,
    @SerializedName("weather_code") val codigo: Int? = null,
    @SerializedName("wind_speed_10m") val viento: Double? = null,
    @SerializedName("wind_direction_10m") val direccion: Int? = null
)

data class HourlyDto(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m") val temperatura: List<Double?> = emptyList()
)
