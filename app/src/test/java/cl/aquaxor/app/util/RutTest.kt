package cl.aquaxor.app.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RutTest {
    @Test fun `normalizar quita puntos y guion y pone K en mayuscula`() {
        assertEquals("111111111", Rut.normalizar("11.111.111-1"))
        assertEquals("12345678K", Rut.normalizar("12.345.678-k"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["111111111", "222222222", "333333333"])
    fun `RUT de demostracion son validos`(rut: String) = assertTrue(Rut.esValido(rut))

    @Test fun `RUT con digito verificador incorrecto es invalido`() = assertFalse(Rut.esValido("111111112"))
    @Test fun `RUT demasiado corto es invalido`() = assertFalse(Rut.esValido("1234567"))
    @Test fun `RUT con letras en el cuerpo es invalido`() = assertFalse(Rut.esValido("1A1111111"))
}
