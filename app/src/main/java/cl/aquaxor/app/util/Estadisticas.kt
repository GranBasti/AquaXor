package cl.aquaxor.app.util

import cl.aquaxor.app.model.ConteoEstado
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.PuntoMuestra
import cl.aquaxor.app.model.RangoFecha
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Una barra del gráfico semanal: etiqueta del día y total de individuos contados. */
data class BarraDia(val etiqueta: String, val total: Int)

/** Cálculos puros para los gráficos e indicadores (fáciles de probar sin Android). */
object Estadisticas {

    /** Suma de individuos por día para los últimos [dias] días, incluido [hoy]. Días sin datos = 0. */
    fun individuosPorDia(
        puntos: List<PuntoMuestra>,
        hoy: LocalDate = LocalDate.now(),
        dias: Int = 7,
        zona: ZoneId = ZoneId.systemDefault()
    ): List<BarraDia> {
        require(dias > 0) { "dias debe ser positivo" }
        val porDia = puntos.groupBy { Instant.ofEpochMilli(it.fechaHora).atZone(zona).toLocalDate() }
            .mapValues { (_, v) -> v.sumOf { it.cantidad } }
        return (dias - 1 downTo 0).map { atras ->
            val dia = hoy.minusDays(atras.toLong())
            BarraDia(
                etiqueta = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-CL"))
                    .replaceFirstChar { it.uppercase() }.take(3),
                total = porDia[dia] ?: 0
            )
        }
    }

    /** Porcentaje entero 0..100; 0 si el total es 0. */
    fun porcentaje(parte: Int, total: Int): Int =
        if (total <= 0) 0 else ((parte * 100.0) / total).toInt().coerceIn(0, 100)

    /** Fracción 0f..1f para barras de progreso. */
    fun fraccion(parte: Int, total: Int): Float =
        if (total <= 0) 0f else (parte.toFloat() / total).coerceIn(0f, 1f)

    /** Cantidad de muestras de [estado] (0 si no aparece en la lista). */
    fun cantidadDe(estado: EstadoRevision, conteos: List<ConteoEstado>): Int =
        conteos.firstOrNull { it.estado == estado }?.total ?: 0

    /** Promedio de la lista; 0.0 si está vacía. */
    fun promedio(valores: List<Int>): Double =
        if (valores.isEmpty()) 0.0 else valores.sum().toDouble() / valores.size

    /** Porcentaje de muestras ya sincronizadas con el servidor. */
    fun porcentajeSincronizado(total: Int, sinSincronizar: Int): Int =
        porcentaje((total - sinSincronizar).coerceAtLeast(0), total)

    /** Cuántas muestras caen en cada rango de fechas (para mostrar el número en los filtros). */
    fun conteoPorRango(puntos: List<PuntoMuestra>, ahora: Long = System.currentTimeMillis()): Map<RangoFecha, Int> =
        RangoFecha.entries.associateWith { r ->
            val desde = r.desdeMillis(ahora)
            puntos.count { it.fechaHora >= desde }
        }
}
