package cl.aquaxor.app.util

object Rut {
    /** Deja solo dígitos y K en mayúscula: "11.111.111-1" -> "111111111". */
    fun normalizar(texto: String): String =
        texto.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

    /** Valida largo y dígito verificador (módulo 11). */
    fun esValido(rutNormalizado: String): Boolean {
        if (rutNormalizado.length !in 8..9) return false
        val cuerpo = rutNormalizado.dropLast(1)
        val dv = rutNormalizado.last()
        if (!cuerpo.all { it.isDigit() }) return false
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - (suma % 11)
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + resto)
        }
        return dv == esperado
    }
}
