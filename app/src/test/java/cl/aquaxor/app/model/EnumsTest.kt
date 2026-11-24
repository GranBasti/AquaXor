package cl.aquaxor.app.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnumsTest {
    @Test fun `solo operador y administrador registran muestras`() {
        assertTrue(Rol.OPERADOR.puedeRegistrar())
        assertTrue(Rol.ADMINISTRADOR.puedeRegistrar())
        assertFalse(Rol.ANALISTA.puedeRegistrar())
    }

    @Test fun `solo administrador elimina y administra`() {
        assertTrue(Rol.ADMINISTRADOR.puedeEliminar())
        assertFalse(Rol.OPERADOR.puedeEliminar())
        assertTrue(Rol.ADMINISTRADOR.esAdministrador())
        assertFalse(Rol.ANALISTA.esAdministrador())
    }

    @Test fun `operador solo puede usar Pendiente y En revision`() {
        assertEquals(listOf(EstadoRevision.PENDIENTE, EstadoRevision.EN_REVISION), Rol.OPERADOR.estadosPermitidos())
    }

    @Test fun `analista y administrador pueden usar todos los estados`() {
        assertEquals(EstadoRevision.entries, Rol.ANALISTA.estadosPermitidos())
        assertEquals(4, Rol.ADMINISTRADOR.estadosPermitidos().size)
    }
}
