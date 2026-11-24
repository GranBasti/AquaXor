package cl.aquaxor.app.data.network

import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.MuestraResumen
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class StoreFalso(var pendientes: List<MuestraResumen>) : PendientesStore {
    var marcadas: List<Long> = emptyList()
    override suspend fun pendientesDeSincronizar() = pendientes
    override suspend fun marcarSincronizadas(ids: List<Long>): Int { marcadas = ids; return ids.size }
}

class SincronizadorInsForgeTest {
    private lateinit var server: MockWebServer

    private fun resumen(id: Long) = MuestraResumen(
        Muestra(id = id, centroId = 1, trenId = 1, lineaId = 1, usuarioId = 1, fechaHora = 1_700_000_000_000,
            tramo = "A", profundidadM = 3.0, cantidadIndividuos = 120),
        "Centro Norte", "Tren 1", "Línea 1", "Ana", 0, 0
    )

    @BeforeEach fun preparar() { server = MockWebServer().also { it.start() } }
    @AfterEach fun cerrar() = server.shutdown()

    private fun api() = RetrofitProvider.insForge(server.url("/").toString(), "token-prueba")

    @Test fun `sin configuracion no llama a la red`() = runTest {
        val r = SincronizadorInsForge(null, StoreFalso(listOf(resumen(1)))).sincronizar()
        assertEquals(ResultadoSync.NoConfigurado, r)
        assertEquals(0, server.requestCount)
    }

    @Test fun `sin pendientes no envia nada`() = runTest {
        val r = SincronizadorInsForge(api(), StoreFalso(emptyList())).sincronizar()
        assertEquals(ResultadoSync.SinPendientes, r)
        assertEquals(0, server.requestCount)
    }

    @Test fun `envia un arreglo JSON con Bearer y marca como sincronizadas`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody("[]").addHeader("Content-Type", "application/json"))
        val store = StoreFalso(listOf(resumen(1), resumen(2)))
        val r = SincronizadorInsForge(api(), store).sincronizar()

        assertEquals(ResultadoSync.Exito(2), r)
        assertEquals(listOf(1L, 2L), store.marcadas)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/database/records/muestras", req.requestUrl!!.encodedPath)
        assertEquals("Bearer token-prueba", req.getHeader("Authorization"))
        val cuerpo = req.body.readUtf8()
        assertTrue(cuerpo.startsWith("["), "el cuerpo debe ser un arreglo JSON")
        assertTrue(cuerpo.contains("\"id_local\":1") && cuerpo.contains("\"id_local\":2"))
        assertTrue(cuerpo.contains("\"cantidad_individuos\":120"))
    }

    @Test fun `si el servidor rechaza no se marcan como sincronizadas`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val store = StoreFalso(listOf(resumen(1)))
        val r = SincronizadorInsForge(api(), store).sincronizar()
        assertEquals(ResultadoSync.Error("El servidor respondió 401"), r)
        assertTrue(store.marcadas.isEmpty())
    }

    @Test fun `falla de red devuelve Error y no marca nada`() = runTest {
        val api = api()
        server.shutdown()
        val store = StoreFalso(listOf(resumen(1)))
        val r = SincronizadorInsForge(api, store).sincronizar()
        assertTrue(r is ResultadoSync.Error)
        assertTrue(store.marcadas.isEmpty())
    }

    @Test fun `aRemota copia los campos de la muestra`() {
        val remota = resumen(5).aRemota()
        assertEquals(5L, remota.idLocal)
        assertEquals("Centro Norte", remota.centro)
        assertEquals("PENDIENTE", remota.estado)
        assertEquals("Ana", remota.operador)
    }
}
