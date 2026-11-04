package cl.aquaxor.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aquaxor.app.data.MessageBus
import cl.aquaxor.app.data.SessionManager
import cl.aquaxor.app.data.network.ConnectivityObserver
import cl.aquaxor.app.data.repository.AquaRepository
import cl.aquaxor.app.util.Rut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val rut: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

class SessionViewModel(
    private val repo: AquaRepository,
    private val session: SessionManager,
    connectivity: ConnectivityObserver,
    private val messages: MessageBus
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    val usuario = session.usuario
    val online: StateFlow<Boolean> = connectivity.isOnline

    init {
        // Avisa con Snackbar cada vez que cambia la conexión a internet.
        viewModelScope.launch {
            connectivity.isOnline.drop(1).collect { conectado ->
                messages.enviar(
                    if (conectado) "Conexión a internet restablecida"
                    else "Sin conexión: los registros se guardan en el dispositivo"
                )
            }
        }
    }

    fun onRutChange(valor: String) = _ui.update { it.copy(rut = valor, error = null) }
    fun onPasswordChange(valor: String) = _ui.update { it.copy(password = valor, error = null) }

    fun login(onExito: () -> Unit) {
        val estado = _ui.value
        if (estado.cargando) return
        val rut = Rut.normalizar(estado.rut)
        if (!Rut.esValido(rut)) {
            _ui.update { it.copy(error = "RUT inválido (ingrésalo sin puntos ni guion)") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, error = null) }
            val usuario = repo.login(rut, estado.password)
            if (usuario == null) {
                _ui.update { it.copy(cargando = false, error = "Credenciales inválidas") }
            } else {
                session.iniciar(usuario)
                _ui.value = LoginUiState()
                messages.enviar("Bienvenido, ${usuario.nombre}")
                onExito()
            }
        }
    }

    fun logout() {
        session.cerrar()
        _ui.value = LoginUiState()
    }
}
