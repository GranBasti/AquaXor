package cl.aquaxor.app.viewmodel

import cl.aquaxor.app.data.network.CurrentDto
import cl.aquaxor.app.data.network.ForecastDto
import cl.aquaxor.app.data.network.WeatherApi
import cl.aquaxor.app.data.network.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClimaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach fun antes() = Dispatchers.setMain(dispatcher)
    @AfterEach fun despues() = Dispatchers.resetMain()

    private class ApiFalsa(val respuesta: () -> ForecastDto) : WeatherApi {
        var llamadas = 0
        override suspend fun pronostico(lat: Double, lon: Double, current: String, hourly: String, dias: Int, zona: String): ForecastDto {
            llamadas++
            return respuesta()
        }
    }

    @Test fun `carga el clima al iniciar`() = runTest(dispatcher) {
        val api = ApiFalsa { ForecastDto(current = CurrentDto(temperatura = 10.0, codigo = 0)) }
        val vm = ClimaViewModel(WeatherRepository(api))
        assertTrue(vm.estado.value.cargando)
        advanceUntilIdle()
        assertFalse(vm.estado.value.cargando)
        assertNotNull(vm.estado.value.clima)
        assertNull(vm.estado.value.error)
    }

    @Test fun `un error de red deja mensaje y sin clima`() = runTest(dispatcher) {
        val api = ApiFalsa { throw java.io.IOException("sin red") }
        val vm = ClimaViewModel(WeatherRepository(api))
        advanceUntilIdle()
        assertNull(vm.estado.value.clima)
        assertTrue(vm.estado.value.error!!.contains("sin red"))
    }

    @Test fun `actualizar vuelve a consultar`() = runTest(dispatcher) {
        val api = ApiFalsa { ForecastDto(current = CurrentDto(temperatura = 10.0)) }
        val vm = ClimaViewModel(WeatherRepository(api))
        advanceUntilIdle()
        vm.actualizar()
        advanceUntilIdle()
        assertEquals(2, api.llamadas)
    }

    private fun assertFalse(b: Boolean) = org.junit.jupiter.api.Assertions.assertFalse(b)
}
