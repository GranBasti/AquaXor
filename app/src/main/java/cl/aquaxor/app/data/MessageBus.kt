package cl.aquaxor.app.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Canal único de mensajes para el Snackbar. Se recoge una sola vez en el NavGraph,
 * así un mensaje emitido justo antes de navegar no se pierde.
 */
class MessageBus {
    private val _mensajes = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun enviar(texto: String) {
        _mensajes.tryEmit(texto)
    }
}
