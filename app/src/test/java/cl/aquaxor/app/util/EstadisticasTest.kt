package cl.aquaxor.app.util

import cl.aquaxor.app.model.ConteoEstado
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.PuntoMuestra
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class EstadisticasTest {
    private val zona = ZoneId.of("America/Santiago")
    private val hoy = LocalDate.of(2026, 11, 23)

    private fun millis(dia: LocalDate, hora: Int = 12) =
        dia.atTime(hora, 0).atZone(zona).toInstant().toEpochMilli()

    @Test fun `individuosPorDia entrega 7 barras y el ultimo es hoy`() {
        val barras = Estadisticas.individuosPorDia(emptyList(), hoy, 7, zona)
        assertEquals(7, barras.size)
        assertEquals("Lun", barras.last().etiqueta) // 23-nov-2026 es lunes
        assertEquals(0, barras.sumOf { it.total })
    }

    @Test fun `individuosPorDia suma las muestras del mismo dia`() {
        val puntos = listOf(
            PuntoMuestra(millis(hoy, 9), 100),
            PuntoMuestra(millis(hoy, 15), 50),
            PuntoMuestra(millis(hoy.minusDays(2)), 30)
        )
        val barras = Estadisticas.individuosPorDia(puntos, hoy, 7, zona)
        assertEquals(150, barras.last().total)
        assertEquals(30, barras[4].total)
    }

    @Test fun `individuosPorDia ignora datos fuera de la ventana`() {
        val barras = Estadisticas.individuosPorDia(listOf(PuntoMuestra(millis(hoy.minusDays(30)), 999)), hoy, 7, zona)
        assertEquals(0, barras.sumOf { it.total })
    }

    @Test fun `individuosPorDia rechaza dias no positivos`() {
        assertThrows(IllegalArgumentException::class.java) { Estadisticas.individuosPorDia(emptyList(), hoy, 0, zona) }
    }

    @Test fun `porcentaje y fraccion manejan total cero`() {
        assertEquals(0, Estadisticas.porcentaje(5, 0))
        assertEquals(0f, Estadisticas.fraccion(5, 0))
    }

    @Test fun `porcentaje calcula valores normales y limita a 100`() {
        assertEquals(50, Estadisticas.porcentaje(1, 2))
        assertEquals(100, Estadisticas.porcentaje(5, 2))
        assertEquals(0.25f, Estadisticas.fraccion(1, 4))
    }

    @Test fun `cantidadDe devuelve cero si el estado no esta`() {
        val c = listOf(ConteoEstado(EstadoRevision.APROBADA, 4))
        assertEquals(4, Estadisticas.cantidadDe(EstadoRevision.APROBADA, c))
        assertEquals(0, Estadisticas.cantidadDe(EstadoRevision.OBSERVADA, c))
    }

    @Test fun `promedio de lista vacia es cero`() {
        assertEquals(0.0, Estadisticas.promedio(emptyList()))
        assertEquals(2.5, Estadisticas.promedio(listOf(1, 2, 3, 4)))
    }

    @Test fun `porcentajeSincronizado`() {
        assertEquals(75, Estadisticas.porcentajeSincronizado(4, 1))
        assertEquals(0, Estadisticas.porcentajeSincronizado(0, 0))
        assertEquals(0, Estadisticas.porcentajeSincronizado(2, 5))
    }

    @Test fun `conteoPorRango cuenta muestras por ventana de tiempo`() {
        val ahora = System.currentTimeMillis()
        val dia = 86_400_000L
        val puntos = listOf(
            PuntoMuestra(ahora - 1_000, 10),
            PuntoMuestra(ahora - 3 * dia, 10),
            PuntoMuestra(ahora - 20 * dia, 10),
            PuntoMuestra(ahora - 90 * dia, 10)
        )
        val c = Estadisticas.conteoPorRango(puntos, ahora)
        assertEquals(4, c[cl.aquaxor.app.model.RangoFecha.TODOS])
        assertEquals(2, c[cl.aquaxor.app.model.RangoFecha.SIETE_DIAS])
        assertEquals(3, c[cl.aquaxor.app.model.RangoFecha.TREINTA_DIAS])
    }
}
