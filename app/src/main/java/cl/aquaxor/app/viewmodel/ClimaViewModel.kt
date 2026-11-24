package cl.aquaxor.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aquaxor.app.data.network.Clima
import cl.aquaxor.app.data.network.ResultadoClima
import cl.aquaxor.app.data.network.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClimaUiState(
    val cargando: Boolean = false,
    val clima: Clima? = null,
    val error: String? = null
)

class ClimaViewModel(private val repo: WeatherRepository) : ViewModel() {
    private val _estado = MutableStateFlow(ClimaUiState())
    val estado: StateFlow<ClimaUiState> = _estado.asStateFlow()

    init { actualizar() }

    fun actualizar() {
        if (_estado.value.cargando) return
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.obtener()) {
                is ResultadoClima.Exito -> _estado.value = ClimaUiState(clima = r.clima)
                is ResultadoClima.Error -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }
}
