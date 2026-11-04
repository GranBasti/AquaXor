package cl.aquaxor.app.util

import java.security.MessageDigest

object Seguridad {
    private const val SAL = "aquaxor::"

    /** Hash SHA-256 para no guardar contraseñas en texto plano (MVP académico). */
    fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((SAL + password).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
