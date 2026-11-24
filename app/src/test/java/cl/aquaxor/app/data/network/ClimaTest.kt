package cl.aquaxor.app.data.network

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ClimaTest {
    @ParameterizedTest
    @CsvSource("0,Despejado", "3,Nublado", "45,Niebla", "61,Lluvia", "81,Chubascos", "95,Tormenta", "999,Sin datos")
    fun `descripcion segun codigo WMO`(codigo: Int, esperado: String) =
        assertEquals(esperado, descripcionClima(codigo))

    @ParameterizedTest
    @CsvSource("0,N", "45,NE", "90,E", "180,S", "270,O", "350,N", "360,N", "-90,O")
    fun `cardinal de grados`(grados: Int, esperado: String) = assertEquals(esperado, cardinal(grados))

    @Test fun `aptitud buena con viento suave y sin lluvia`() =
        assertEquals(Aptitud.BUENA, evaluarAptitud(10.0, 0.0, 1))

    @Test fun `aptitud precaucion con viento moderado`() =
        assertEquals(Aptitud.PRECAUCION, evaluarAptitud(30.0, 0.0, 2))

    @Test fun `aptitud precaucion con lluvia ligera`() =
        assertEquals(Aptitud.PRECAUCION, evaluarAptitud(5.0, 1.5, 61))

    @Test fun `aptitud no recomendable con temporal`() {
        assertEquals(Aptitud.NO_RECOMENDABLE, evaluarAptitud(45.0, 0.0, 3))
        assertEquals(Aptitud.NO_RECOMENDABLE, evaluarAptitud(5.0, 6.0, 65))
        assertEquals(Aptitud.NO_RECOMENDABLE, evaluarAptitud(5.0, 0.0, 95))
    }

    @Test fun `ForecastDto sin current devuelve null`() = assertNull(ForecastDto().aClima())

    @Test fun `ForecastDto sin temperatura devuelve null`() =
        assertNull(ForecastDto(current = CurrentDto()).aClima())

    @Test fun `ForecastDto completo se convierte a Clima`() {
        val c = ForecastDto(
            current = CurrentDto(temperatura = 11.5, humedad = 80, sensacion = 9.0, precipitation = 0.2,
                codigo = 2, viento = 18.0, direccion = 200),
            hourly = HourlyDto(temperatura = listOf(8.0, null, 12.0))
        ).aClima()
        assertNotNull(c)
        assertEquals(11.5, c!!.temperatura)
        assertEquals("Parcialmente nublado", c.descripcion)
        assertEquals("S", c.direccionCardinal)
        assertEquals(listOf(8.0, 12.0), c.horaTemperatura)
        assertEquals(Aptitud.BUENA, c.aptitud)
    }
}
