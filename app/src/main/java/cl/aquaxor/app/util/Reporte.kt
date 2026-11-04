package cl.aquaxor.app.util

import cl.aquaxor.app.model.MuestraResumen

object Reporte {
    private fun celda(texto: String): String =
        if (texto.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + texto.replace("\"", "\"\"") + "\""
        } else texto

    /** CSV separado por ";" con BOM UTF-8 para que Excel lo abra con tildes. */
    fun csv(filas: List<MuestraResumen>): String {
        val sb = StringBuilder("﻿")
        sb.append(
            listOf(
                "ID", "Fecha y hora", "Centro", "Tren", "Linea", "Tramo", "Profundidad (m)",
                "Talla (mm)", "Individuos", "Estado", "Operador", "Fotos", "Documentos",
                "Sincronizada", "Observaciones"
            ).joinToString(";")
        ).append('\n')
        filas.forEach { f ->
            val m = f.muestra
            sb.append(
                listOf(
                    m.id.toString(),
                    Formatos.fechaHoraIso(m.fechaHora),
                    f.centroNombre,
                    f.trenNombre,
                    f.lineaNombre,
                    m.tramo,
                    Formatos.decimal(m.profundidadM),
                    Formatos.decimal(m.tallaMm),
                    m.cantidadIndividuos.toString(),
                    m.estado.etiqueta,
                    f.operadorNombre,
                    f.totalFotos.toString(),
                    f.totalDocumentos.toString(),
                    if (m.sincronizada) "Si" else "No",
                    m.observaciones
                ).joinToString(";") { celda(it) }
            ).append('\n')
        }
        return sb.toString()
    }
}
