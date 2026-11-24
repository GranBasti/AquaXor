package cl.aquaxor.app.util

import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.MuestraResumen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReporteTest {
    private fun resumen(obs: String = "", sync: Boolean = false) = MuestraResumen(
        muestra = Muestra(id = 7, centroId = 1, trenId = 1, lineaId = 1, usuarioId = 1,
            fechaHora = 0L, tramo = "A", profundidadM = 3.0, tallaMm = 45.5,
            cantidadIndividuos = 120, observaciones = obs, sincronizada = sync),
        centroNombre = "Centro Norte", trenNombre = "Tren 1", lineaNombre = "Línea 2",
        operadorNombre = "Ana", totalFotos = 2, totalDocumentos = 1
    )

    @Test fun `el CSV empieza con BOM y trae encabezado`() {
        val csv = Reporte.csv(emptyList())
        assertTrue(csv.startsWith("﻿ID;Fecha y hora;Centro"))
        assertEquals(1, csv.trim().lines().size)
    }

    @Test fun `una fila por muestra con decimales limpios`() {
        val lineas = Reporte.csv(listOf(resumen())).trim().lines()
        assertEquals(2, lineas.size)
        assertTrue(lineas[1].startsWith("7;"))
        assertTrue(lineas[1].contains(";3;45.5;120;"))
    }

    @Test fun `observaciones con punto y coma se entrecomillan`() {
        val csv = Reporte.csv(listOf(resumen(obs = "uno; \"dos\"")))
        assertTrue(csv.contains("\"uno; \"\"dos\"\"\""))
    }

    @Test fun `marca Si o No segun sincronizacion`() {
        assertTrue(Reporte.csv(listOf(resumen(sync = true))).contains(";Si;"))
        assertTrue(Reporte.csv(listOf(resumen(sync = false))).contains(";No;"))
    }
}
