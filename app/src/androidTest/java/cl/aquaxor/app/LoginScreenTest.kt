package cl.aquaxor.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import cl.aquaxor.app.ui.screens.LoginContent
import cl.aquaxor.app.ui.theme.AquaXorTheme
import cl.aquaxor.app.viewmodel.LoginUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Pruebas funcionales de interfaz (Compose UI Test). Se ejecutan en emulador o dispositivo. */
class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun mostrar(
        estado: LoginUiState = LoginUiState(),
        onRut: (String) -> Unit = {},
        onPass: (String) -> Unit = {},
        onLogin: () -> Unit = {}
    ) = compose.setContent {
        AquaXorTheme { LoginContent(estado, SnackbarHostState(), onRut, onPass, onLogin) }
    }

    @Test fun muestraTituloYUsuariosDeDemostracion() {
        mostrar()
        compose.onNodeWithText("AquaXor").assertExists()
        compose.onNodeWithText("Administrador: 111111111 / admin123").assertExists()
    }

    @Test fun botonDeshabilitadoConCamposVacios() {
        mostrar()
        compose.onNodeWithTag("boton_login").assertIsNotEnabled()
    }

    @Test fun botonHabilitadoConCamposLlenos() {
        mostrar(LoginUiState(rut = "111111111", password = "admin123"))
        compose.onNodeWithTag("boton_login").assertIsEnabled()
    }

    @Test fun escribirEnRutNotificaAlViewModel() {
        var recibido = ""
        mostrar(onRut = { recibido = it })
        compose.onNodeWithTag("campo_rut").performTextInput("1")
        assertEquals("1", recibido)
    }

    @Test fun pulsarIniciarSesionLlamaAlCallback() {
        var llamado = 0
        mostrar(LoginUiState(rut = "111111111", password = "admin123"), onLogin = { llamado++ })
        compose.onNodeWithTag("boton_login").performClick()
        assertEquals(1, llamado)
    }

    @Test fun muestraElMensajeDeError() {
        mostrar(LoginUiState(rut = "1", password = "x", error = "Credenciales inválidas"))
        compose.onNodeWithText("Credenciales inválidas").assertExists()
    }
}
