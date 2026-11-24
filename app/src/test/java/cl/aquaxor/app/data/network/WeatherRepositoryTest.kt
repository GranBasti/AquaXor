package cl.aquaxor.app.data.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Prueba funcional de Retrofit: se levanta un servidor falso y se verifica request y parseo. */
class WeatherRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: WeatherRepository

    private val json = """
      {"latitude":-41.47,"longitude":-72.93,
       "current":{"time":"2026-11-23T12:00","temperature_2m":12.3,"relative_humidity_2m":71,
                  "apparent_temperature":10.1,"precipitation":0.0,"weather_code":3,
                  "wind_speed_10m":14.4,"wind_direction_10m":225},
       "hourly":{"time":["2026-11-23T00:00","2026-11-23T01:00"],"temperature_2m":[9.0,9.4]}}
    """.trimIndent()

    @BeforeEach fun preparar() {
        server = MockWebServer().also { it.start() }
        repo = WeatherRepository(RetrofitProvider.weather(server.url("/").toString()))
    }

    @AfterEach fun cerrar() = server.shutdown()

    @Test fun `parsea la respuesta y arma la consulta correcta`() = runTest {
        server.enqueue(MockResponse().setBody(json).addHeader("Content-Type", "application/json"))
        val r = repo.obtener()
        assertTrue(r is ResultadoClima.Exito)
        val clima = (r as ResultadoClima.Exito).clima
        assertEquals(12.3, clima.temperatura)
        assertEquals("Nublado", clima.descripcion)
        assertEquals("SO", clima.direccionCardinal)
        assertEquals(2, clima.horaTemperatura.size)

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/forecast", req.requestUrl!!.encodedPath)
        assertEquals("-41.4717", req.requestUrl!!.queryParameter("latitude"))
        assertEquals("-72.9369", req.requestUrl!!.queryParameter("longitude"))
        assertTrue(req.requestUrl!!.queryParameter("current")!!.contains("wind_speed_10m"))
    }

    @Test fun `error HTTP 500 se informa como Error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val r = repo.obtener()
        assertTrue(r is ResultadoClima.Error)
        assertTrue((r as ResultadoClima.Error).mensaje.startsWith("No se pudo consultar el clima"))
    }

    @Test fun `respuesta sin datos actuales se informa como Error`() = runTest {
        server.enqueue(MockResponse().setBody("{}").addHeader("Content-Type", "application/json"))
        val r = repo.obtener()
        assertEquals(ResultadoClima.Error("La API no entregó datos actuales"), r)
    }

    @Test fun `servidor caido se informa como Error`() = runTest {
        server.shutdown()
        assertTrue(repo.obtener() is ResultadoClima.Error)
    }
}
