package cl.aquaxor.app

import android.app.Application
import kotlinx.coroutines.launch

class AquaXorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Carga usuarios, catálogos y muestras ficticias la primera vez.
        container.appScope.launch { container.repository.sembrarSiVacio() }
    }
}
