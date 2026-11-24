package cl.aquaxor.app.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Construye los clientes Retrofit. `baseUrl` es parámetro para poder apuntar a MockWebServer en las pruebas. */
object RetrofitProvider {

    private fun cliente(interceptor: Interceptor? = null): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .apply { interceptor?.let(::addInterceptor) }
            .build()

    private fun retrofit(baseUrl: String, http: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun weather(baseUrl: String = WeatherApi.BASE_URL): WeatherApi =
        retrofit(baseUrl, cliente()).create(WeatherApi::class.java)

    fun insForge(baseUrl: String, token: String): InsForgeApi {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return retrofit(url, cliente(bearer(token))).create(InsForgeApi::class.java)
    }

    fun bearer(token: String): Interceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        )
    }
}
