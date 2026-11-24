package cl.aquaxor.app.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class SeguridadTest {
    @Test fun `el hash es determinista`() = assertEquals(Seguridad.hash("admin123"), Seguridad.hash("admin123"))
    @Test fun `contrasenas distintas dan hash distinto`() = assertNotEquals(Seguridad.hash("a"), Seguridad.hash("b"))
    @Test fun `el hash SHA-256 tiene 64 caracteres hexadecimales`() {
        val h = Seguridad.hash("x")
        assertEquals(64, h.length)
        assertEquals(true, h.all { it in "0123456789abcdef" })
    }
    @Test fun `el hash no contiene la contrasena en texto plano`() =
        assertEquals(false, Seguridad.hash("admin123").contains("admin123"))
}
