package cl.aquaxor.app

import android.content.Context
import cl.aquaxor.app.data.MessageBus
import cl.aquaxor.app.data.SessionManager
import cl.aquaxor.app.data.local.AquaDatabase
import cl.aquaxor.app.data.network.ConnectivityObserver
import cl.aquaxor.app.data.repository.AquaRepository
import cl.aquaxor.app.data.storage.FileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Inyección de dependencias manual (suficiente para este MVP). */
class AppContainer(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database = AquaDatabase.crear(context)

    val repository = AquaRepository(database)
    val storage = FileStorage(context)
    val connectivity = ConnectivityObserver(context, appScope)
    val session = SessionManager()
    val messages = MessageBus()
}
