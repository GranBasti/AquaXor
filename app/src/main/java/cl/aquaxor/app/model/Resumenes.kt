package cl.aquaxor.app.model

import androidx.room.Embedded

/** Resultado del JOIN de muestras con catálogos, operador y cantidad de adjuntos. */
data class MuestraResumen(
    @Embedded val muestra: Muestra,
    val centroNombre: String,
    val trenNombre: String,
    val lineaNombre: String,
    val operadorNombre: String,
    val totalFotos: Int,
    val totalDocumentos: Int
)

data class ConteoEstado(
    val estado: EstadoRevision,
    val total: Int
)

data class ResumenGeneral(
    val totalMuestras: Int = 0,
    val pendientes: Int = 0,
    val individuosHoy: Int = 0,
    val sinSincronizar: Int = 0
)

enum class RangoFecha(val etiqueta: String) {
    TODOS("Todo"),
    HOY("Hoy"),
    SIETE_DIAS("7 días"),
    TREINTA_DIAS("30 días");

    fun desdeMillis(ahora: Long = System.currentTimeMillis()): Long = when (this) {
        TODOS -> 0L
        HOY -> cl.aquaxor.app.util.Formatos.inicioDelDia()
        SIETE_DIAS -> ahora - 7 * 86_400_000L
        TREINTA_DIAS -> ahora - 30 * 86_400_000L
    }
}

data class FiltrosHistorial(
    val texto: String = "",
    val estado: EstadoRevision? = null,
    val rango: RangoFecha = RangoFecha.TODOS,
    val centroId: Long? = null
)
