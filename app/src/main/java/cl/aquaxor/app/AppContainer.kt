package cl.aquaxor.app

import android.content.Context
import cl.aquaxor.app.data.MessageBus
import cl.aquaxor.app.data.SessionManager
import cl.aquaxor.app.data.local.AquaDatabase
import cl.aquaxor.app.data.network.ConnectivityObserver
import cl.aquaxor.app.data.network.RetrofitProvider
import cl.aquaxor.app.data.network.SincronizadorInsForge
import cl.aquaxor.app.data.network.WeatherRepository
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

    /** Clima: Retrofit + Open-Meteo. */
    val weather = WeatherRepository(RetrofitProvider.weather())

    /** Backend InsForge: si no hay URL/clave en gradle.properties, queda en null (modo solo local). */
    val sincronizador = SincronizadorInsForge(
        api = if (BuildConfig.INSFORGE_URL.isNotBlank() && BuildConfig.INSFORGE_KEY.isNotBlank())
            RetrofitProvider.insForge(BuildConfig.INSFORGE_URL, BuildConfig.INSFORGE_KEY) else null,
        store = repository
    )
}
