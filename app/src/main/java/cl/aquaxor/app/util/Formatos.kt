package cl.aquaxor.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatos {
    private val locale: Locale = Locale.forLanguageTag("es-CL")
    private val fechaFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
    private val horaFmt = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val fechaHoraFmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", locale)
    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", locale)
    private val archivoFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", locale)

    private fun local(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())

    fun fecha(millis: Long): String = local(millis).format(fechaFmt)
    fun hora(millis: Long): String = local(millis).format(horaFmt)
    fun fechaHora(millis: Long): String = local(millis).format(fechaHoraFmt)
    fun fechaHoraIso(millis: Long): String = local(millis).format(isoFmt)
    fun marcaArchivo(millis: Long = System.currentTimeMillis()): String = local(millis).format(archivoFmt)

    fun horaDe(millis: Long): Int = local(millis).hour
    fun minutoDe(millis: Long): Int = local(millis).minute

    fun inicioDelDia(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** El DatePicker de Material 3 trabaja con medianoche UTC. */
    fun fechaParaPicker(millis: Long): Long =
        local(millis).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /** Cambia solo el día de [actual] usando la fecha elegida en el DatePicker (UTC). */
    fun conFecha(actual: Long, fechaUtcMillis: Long): Long {
        val dia = Instant.ofEpochMilli(fechaUtcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        val hora = local(actual).toLocalTime()
        return LocalDateTime.of(dia, hora).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** Cambia solo la hora de [actual]. */
    fun conHora(actual: Long, hora: Int, minuto: Int): Long {
        val dia = local(actual).toLocalDate()
        return LocalDateTime.of(dia, LocalTime.of(hora, minuto))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** 4.0 -> "4", 4.5 -> "4.5", null -> "". */
    fun decimal(valor: Double?): String = when {
        valor == null -> ""
        valor % 1.0 == 0.0 -> valor.toLong().toString()
        else -> valor.toString()
    }
}
