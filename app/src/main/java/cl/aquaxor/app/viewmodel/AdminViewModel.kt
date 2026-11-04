package cl.aquaxor.app.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aquaxor.app.data.MessageBus
import cl.aquaxor.app.data.SessionManager
import cl.aquaxor.app.data.repository.AquaRepository
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Rol
import cl.aquaxor.app.model.Tren
import cl.aquaxor.app.model.Usuario
import cl.aquaxor.app.util.Rut
import cl.aquaxor.app.util.Seguridad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** CRUD de usuarios y catálogos (centros, trenes y líneas) para el rol Administrador. */
class AdminViewModel(
    private val repo: AquaRepository,
    private val session: SessionManager,
    private val messages: MessageBus
) : ViewModel() {

    private val compartir = SharingStarted.WhileSubscribed(5_000)

    val centros: StateFlow<List<Centro>> = repo.centros().stateIn(viewModelScope, compartir, emptyList())
    val trenes: StateFlow<List<Tren>> = repo.trenes().stateIn(viewModelScope, compartir, emptyList())
    val lineas: StateFlow<List<Linea>> = repo.lineas().stateIn(viewModelScope, compartir, emptyList())
    val usuarios: StateFlow<List<Usuario>> = repo.usuarios().stateIn(viewModelScope, compartir, emptyList())

    private fun ejecutar(exito: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                bloque()
                messages.enviar(exito)
            } catch (e: SQLiteConstraintException) {
                messages.enviar("No se pudo completar: el dato está en uso o ya existe")
            } catch (e: Exception) {
                messages.enviar("Ocurrió un error: ${e.message ?: "desconocido"}")
            }
        }
    }

    private fun nombreValido(nombre: String): Boolean {
        if (nombre.isBlank()) {
            messages.enviar("El nombre no puede estar vacío")
            return false
        }
        return true
    }

    // ---- Centros ----
    fun guardarCentro(id: Long?, nombre: String) {
        if (!nombreValido(nombre)) return
        ejecutar("Centro guardado") { repo.guardarCentro(Centro(id = id ?: 0L, nombre = nombre.trim())) }
    }

    fun eliminarCentro(centro: Centro) = ejecutar("Centro eliminado") { repo.eliminarCentro(centro) }

    // ---- Trenes ----
    fun guardarTren(id: Long?, centroId: Long?, nombre: String) {
        if (!nombreValido(nombre)) return
        if (centroId == null) {
            messages.enviar("Selecciona un centro")
            return
        }
        ejecutar("Tren guardado") { repo.guardarTren(Tren(id = id ?: 0L, centroId = centroId, nombre = nombre.trim())) }
    }

    fun eliminarTren(tren: Tren) = ejecutar("Tren eliminado") { repo.eliminarTren(tren) }

    // ---- Líneas ----
    fun guardarLinea(id: Long?, trenId: Long?, nombre: String) {
        if (!nombreValido(nombre)) return
        if (trenId == null) {
            messages.enviar("Selecciona un tren")
            return
        }
        ejecutar("Línea guardada") { repo.guardarLinea(Linea(id = id ?: 0L, trenId = trenId, nombre = nombre.trim())) }
    }

    fun eliminarLinea(linea: Linea) = ejecutar("Línea eliminada") { repo.eliminarLinea(linea) }

    // ---- Usuarios ----
    fun guardarUsuario(existente: Usuario?, rutTexto: String, nombre: String, rol: Rol, password: String, activo: Boolean) {
        if (!nombreValido(nombre)) return
        val rut = Rut.normalizar(rutTexto)
        if (!Rut.esValido(rut)) {
            messages.enviar("RUT inválido (sin puntos ni guion)")
            return
        }
        if (existente == null && password.length < 4) {
            messages.enviar("La contraseña debe tener al menos 4 caracteres")
            return
        }
        val hash = if (password.isBlank()) existente?.passwordHash.orEmpty() else Seguridad.hash(password)
        ejecutar("Usuario guardado") {
            repo.guardarUsuario(
                Usuario(
                    id = existente?.id ?: 0L,
                    rut = rut,
                    nombre = nombre.trim(),
                    passwordHash = hash,
                    rol = rol,
                    activo = activo
                )
            )
        }
    }

    fun eliminarUsuario(usuario: Usuario) {
        if (usuario.id == session.usuario.value?.id) {
            messages.enviar("No puedes eliminar tu propio usuario")
            return
        }
        ejecutar("Usuario eliminado") { repo.eliminarUsuario(usuario) }
    }
}
