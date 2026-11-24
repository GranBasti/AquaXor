package cl.aquaxor.app.data.network

/** Condiciones meteorológicas ya interpretadas, listas para la interfaz. */
data class Clima(
    val temperatura: Double,
    val sensacion: Double,
    val humedad: Int,
    val vientoKmh: Double,
    val direccionGrados: Int,
    val precipitacionMm: Double,
    val codigo: Int,
    val horaTemperatura: List<Double> = emptyList()
) {
    val descripcion: String get() = descripcionClima(codigo)
    val direccionCardinal: String get() = cardinal(direccionGrados)
    val aptitud: Aptitud get() = evaluarAptitud(vientoKmh, precipitacionMm, codigo)
}

enum class Aptitud(val etiqueta: String, val detalle: String) {
    BUENA("Condiciones favorables", "Buen momento para muestrear en las líneas."),
    PRECAUCION("Precaución", "Viento o lluvia moderados: extremar medidas de seguridad."),
    NO_RECOMENDABLE("No recomendable", "Condiciones adversas: evaluar posponer la faena.")
}

/** Traduce el código WMO de Open-Meteo a texto. */
fun descripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    95, 96, 99 -> "Tormenta"
    else -> "Sin datos"
}

/** Grados (0-360) a punto cardinal de 8 vientos. */
fun cardinal(grados: Int): String {
    val puntos = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    val g = ((grados % 360) + 360) % 360
    return puntos[((g + 22) / 45) % 8]
}

/** Regla simple para faenas en balsas: viento, lluvia y tormenta. */
fun evaluarAptitud(vientoKmh: Double, precipitacionMm: Double, codigo: Int): Aptitud = when {
    codigo in 95..99 || vientoKmh >= 40 || precipitacionMm >= 5.0 -> Aptitud.NO_RECOMENDABLE
    vientoKmh >= 25 || precipitacionMm >= 1.0 || codigo in 61..67 || codigo in 80..82 -> Aptitud.PRECAUCION
    else -> Aptitud.BUENA
}

/** Convierte la respuesta de la API; devuelve null si faltan los datos actuales. */
fun ForecastDto.aClima(): Clima? {
    val c = current ?: return null
    return Clima(
        temperatura = c.temperatura ?: return null,
        sensacion = c.sensacion ?: c.temperatura,
        humedad = c.humedad ?: 0,
        vientoKmh = c.viento ?: 0.0,
        direccionGrados = c.direccion ?: 0,
        precipitacionMm = c.precipitation ?: 0.0,
        codigo = c.codigo ?: -1,
        horaTemperatura = hourly?.temperatura?.filterNotNull() ?: emptyList()
    )
}
