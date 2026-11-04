package cl.aquaxor.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.aquaxor.app.AppContainer

class AquaViewModelFactory(private val c: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SessionViewModel::class.java) ->
            SessionViewModel(c.repository, c.session, c.connectivity, c.messages) as T
        modelClass.isAssignableFrom(SampleViewModel::class.java) ->
            SampleViewModel(c.repository, c.storage, c.session, c.connectivity, c.messages) as T
        modelClass.isAssignableFrom(AdminViewModel::class.java) ->
            AdminViewModel(c.repository, c.session, c.messages) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
