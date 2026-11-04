package cl.aquaxor.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aquaxor.app.data.MessageBus
import cl.aquaxor.app.data.SessionManager
import cl.aquaxor.app.data.network.ConnectivityObserver
import cl.aquaxor.app.data.repository.AquaRepository
import cl.aquaxor.app.data.storage.FileStorage
import cl.aquaxor.app.model.Adjunto
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.FiltrosHistorial
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.MuestraResumen
import cl.aquaxor.app.model.RangoFecha
import cl.aquaxor.app.model.ResumenGeneral
import cl.aquaxor.app.model.TipoAdjunto
import cl.aquaxor.app.model.Tren
import cl.aquaxor.app.model.puedeEliminar
import cl.aquaxor.app.util.Formatos
import cl.aquaxor.app.util.Reporte
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/** Adjunto del formulario aún no guardado (nuevo) o ya guardado en la base (nuevo = false). */
data class AdjuntoBorrador(
    val id: Long = 0,
    val tipo: TipoAdjunto,
    val ruta: String,
    val nombre: String,
    val nuevo: Boolean
)

/** Estado del formulario "Nueva muestra / Editar muestra" y de la pantalla de evidencia. */
data class BorradorMuestra(
    val original: Muestra? = null,
    val centroId: Long? = null,
    val trenId: Long? = null,
    val lineaId: Long? = null,
    val fechaHora: Long = System.currentTimeMillis(),
    val tramo: String = "",
    val profundidad: String = "",
    val talla: String = "",
    val cantidad: Int = 0,
    val observaciones: String = "",
    val adjuntos: List<AdjuntoBorrador> = emptyList(),
    val eliminados: List<AdjuntoBorrador> = emptyList(),
    val errores: Map<String, String> = emptyMap()
) {
    val editando: Boolean get() = original != null
}

@OptIn(ExperimentalCoroutinesApi::class)
class SampleViewModel(
    private val repo: AquaRepository,
    private val storage: FileStorage,
    private val session: SessionManager,
    connectivity: ConnectivityObserver,
    private val messages: MessageBus
) : ViewModel() {

    private val compartir = SharingStarted.WhileSubscribed(5_000)

    val online: StateFlow<Boolean> = connectivity.isOnline

    // ====================== Formulario (borrador) ======================
    private val _borrador = MutableStateFlow(BorradorMuestra())
    val borrador: StateFlow<BorradorMuestra> = _borrador.asStateFlow()

    private var archivoFotoPendiente: File? = null

    val centros: StateFlow<List<Centro>> =
        repo.centros().stateIn(viewModelScope, compartir, emptyList())

    val trenes: StateFlow<List<Tren>> = _borrador.map { it.centroId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Tren>()) else repo.trenesDe(id) }
        .stateIn(viewModelScope, compartir, emptyList())

    val lineas: StateFlow<List<Linea>> = _borrador.map { it.trenId }
        .distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Linea>()) else repo.lineasDe(id) }
        .stateIn(viewModelScope, compartir, emptyList())

    fun nuevaMuestra() {
        descartarArchivosNuevos()
        _borrador.value = BorradorMuestra()
    }

    /** Descarta el borrador y borra los archivos que aún no se habían guardado. */
    fun cancelar() = nuevaMuestra()

    fun cargarParaEditar(id: Long, onListo: () -> Unit) {
        viewModelScope.launch {
            val m = repo.obtener(id) ?: return@launch
            val adjuntos = repo.adjuntosDe(id)
            descartarArchivosNuevos()
            _borrador.value = BorradorMuestra(
                original = m,
                centroId = m.centroId,
                trenId = m.trenId,
                lineaId = m.lineaId,
                fechaHora = m.fechaHora,
                tramo = m.tramo,
                profundidad = Formatos.decimal(m.profundidadM),
                talla = Formatos.decimal(m.tallaMm),
                cantidad = m.cantidadIndividuos,
                observaciones = m.observaciones,
                adjuntos = adjuntos.map { AdjuntoBorrador(it.id, it.tipo, it.ruta, it.nombre, nuevo = false) }
            )
            onListo()
        }
    }

    fun onCentro(id: Long) = _borrador.update {
        it.copy(centroId = id, trenId = null, lineaId = null, errores = it.errores - "centro")
    }

    fun onTren(id: Long) = _borrador.update {
        it.copy(trenId = id, lineaId = null, errores = it.errores - "tren")
    }

    fun onLinea(id: Long) = _borrador.update { it.copy(lineaId = id, errores = it.errores - "linea") }
    fun onFecha(fechaUtcMillis: Long) = _borrador.update { it.copy(fechaHora = Formatos.conFecha(it.fechaHora, fechaUtcMillis)) }
    fun onHora(hora: Int, minuto: Int) = _borrador.update { it.copy(fechaHora = Formatos.conHora(it.fechaHora, hora, minuto)) }
    fun onTramo(v: String) = _borrador.update { it.copy(tramo = v, errores = it.errores - "tramo") }

    fun onProfundidad(v: String) = _borrador.update {
        it.copy(profundidad = v.filter { c -> c.isDigit() || c == '.' }, errores = it.errores - "profundidad")
    }

    fun onTalla(v: String) = _borrador.update {
        it.copy(talla = v.filter { c -> c.isDigit() || c == '.' }, errores = it.errores - "talla")
    }

    fun onCantidad(valor: Int) = _borrador.update {
        it.copy(cantidad = valor.coerceIn(0, 999_999), errores = it.errores - "cantidad")
    }

    fun sumarCantidad(delta: Int) = onCantidad(_borrador.value.cantidad + delta)
    fun onObservaciones(v: String) = _borrador.update { it.copy(observaciones = v) }

    /** Valida el formulario; deja los errores en el estado para mostrarlos en pantalla. */
    fun validarFormulario(): Boolean {
        val b = _borrador.value
        val errores = buildMap<String, String> {
            if (b.centroId == null) put("centro", "Selecciona un centro")
            if (b.trenId == null) put("tren", "Selecciona un tren")
            if (b.lineaId == null) put("linea", "Selecciona una línea")
            if (b.tramo.isBlank()) put("tramo", "Indica el tramo muestreado")
            if (b.profundidad.isNotBlank() && b.profundidad.toDoubleOrNull() == null) put("profundidad", "Valor inválido")
            if (b.talla.isNotBlank() && b.talla.toDoubleOrNull() == null) put("talla", "Valor inválido")
            if (b.cantidad <= 0) put("cantidad", "Ingresa la cantidad de individuos contados")
        }
        _borrador.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) messages.enviar("Revisa los campos marcados del formulario")
        return errores.isEmpty()
    }

    // ---- Evidencia: cámara, galería y documentos ----
    /** Crea el archivo de destino y devuelve la URI (FileProvider) para la cámara. */
    fun prepararFoto(): Uri {
        val archivo = storage.nuevoArchivoFoto()
        archivoFotoPendiente = archivo
        return storage.uri(archivo.absolutePath)
    }

    fun onFotoResultado(exito: Boolean) {
        val archivo = archivoFotoPendiente
        archivoFotoPendiente = null
        if (archivo == null) return
        if (exito && archivo.exists() && archivo.length() > 0) {
            agregarAdjunto(TipoAdjunto.FOTO, archivo.absolutePath, archivo.name)
            messages.enviar("Fotografía agregada")
        } else {
            archivo.delete()
        }
    }

    /** Copia una imagen de la galería o un documento elegido con el selector del sistema. */
    fun importar(uri: Uri, tipo: TipoAdjunto) {
        viewModelScope.launch {
            val prefijo = if (tipo == TipoAdjunto.FOTO) "GAL" else "DOC"
            val archivo = storage.copiarDesdeUri(uri, prefijo)
            if (archivo == null) {
                messages.enviar("No se pudo importar el archivo")
                return@launch
            }
            agregarAdjunto(tipo, archivo.ruta, archivo.nombre)
            messages.enviar(if (tipo == TipoAdjunto.FOTO) "Imagen agregada desde la galería" else "Documento adjuntado")
        }
    }

    private fun agregarAdjunto(tipo: TipoAdjunto, ruta: String, nombre: String) {
        _borrador.update {
            it.copy(adjuntos = it.adjuntos + AdjuntoBorrador(tipo = tipo, ruta = ruta, nombre = nombre, nuevo = true))
        }
    }

    fun quitarAdjunto(adjunto: AdjuntoBorrador) {
        if (adjunto.nuevo) storage.eliminar(adjunto.ruta)
        _borrador.update {
            it.copy(
                adjuntos = it.adjuntos - adjunto,
                eliminados = if (adjunto.nuevo) it.eliminados else it.eliminados + adjunto
            )
        }
        messages.enviar("Adjunto quitado")
    }

    /** Permite a la UI mostrar un mensaje en el Snackbar. */
    fun avisar(texto: String) = messages.enviar(texto)

    fun uriAdjunto(ruta: String): Uri = storage.uri(ruta)
    fun mimeAdjunto(ruta: String): String = storage.mime(ruta)

    private fun descartarArchivosNuevos() {
        _borrador.value.adjuntos.filter { it.nuevo }.forEach { storage.eliminar(it.ruta) }
        archivoFotoPendiente?.delete()
        archivoFotoPendiente = null
    }

    fun guardar(onGuardada: (Long) -> Unit) {
        if (!validarFormulario()) return
        val b = _borrador.value
        val usuario = session.usuario.value ?: return
        viewModelScope.launch {
            val original = b.original
            val muestra = Muestra(
                id = original?.id ?: 0L,
                centroId = b.centroId!!,
                trenId = b.trenId!!,
                lineaId = b.lineaId!!,
                usuarioId = original?.usuarioId ?: usuario.id,
                fechaHora = b.fechaHora,
                tramo = b.tramo.trim(),
                profundidadM = b.profundidad.toDoubleOrNull(),
                tallaMm = b.talla.toDoubleOrNull(),
                cantidadIndividuos = b.cantidad,
                observaciones = b.observaciones.trim(),
                estado = original?.estado ?: EstadoRevision.PENDIENTE,
                sincronizada = false
            )
            val nuevos = b.adjuntos.filter { it.nuevo }.map {
                Adjunto(muestraId = 0, tipo = it.tipo, ruta = it.ruta, nombre = it.nombre, creadoEn = System.currentTimeMillis())
            }
            try {
                val id = repo.guardarMuestra(muestra, nuevos, b.eliminados.map { it.id })
                b.eliminados.forEach { storage.eliminar(it.ruta) }
                _borrador.value = BorradorMuestra()
                messages.enviar(if (original == null) "Muestra #$id registrada" else "Muestra #$id actualizada")
                onGuardada(id)
            } catch (e: Exception) {
                messages.enviar("No se pudo guardar la muestra")
            }
        }
    }

    // ====================== Historial: búsqueda y filtros ======================
    private val _filtros = MutableStateFlow(FiltrosHistorial())
    val filtros: StateFlow<FiltrosHistorial> = _filtros.asStateFlow()

    val historial: StateFlow<List<MuestraResumen>> = _filtros
        .flatMapLatest { f -> repo.buscar(f.texto.trim(), f.estado, f.rango.desdeMillis(), f.centroId) }
        .stateIn(viewModelScope, compartir, emptyList())

    fun onTextoBusqueda(v: String) = _filtros.update { it.copy(texto = v) }
    fun onFiltroEstado(e: EstadoRevision?) = _filtros.update { it.copy(estado = e) }
    fun onFiltroRango(r: RangoFecha) = _filtros.update { it.copy(rango = r) }
    fun onFiltroCentro(id: Long?) = _filtros.update { it.copy(centroId = id) }
    fun limpiarFiltros() { _filtros.value = FiltrosHistorial() }

    // ====================== Estados de muestra ======================
    private val _estadoSeleccionado = MutableStateFlow<EstadoRevision?>(null)
    val estadoSeleccionado: StateFlow<EstadoRevision?> = _estadoSeleccionado.asStateFlow()

    val muestrasPorEstado: StateFlow<List<MuestraResumen>> = _estadoSeleccionado
        .flatMapLatest { e -> repo.buscar("", e, 0L, null) }
        .stateIn(viewModelScope, compartir, emptyList())

    val conteoPorEstado: StateFlow<Map<EstadoRevision, Int>> = repo.conteoPorEstado()
        .map { lista -> lista.associate { it.estado to it.total } }
        .stateIn(viewModelScope, compartir, emptyMap())

    fun seleccionarEstado(e: EstadoRevision?) {
        _estadoSeleccionado.value = if (_estadoSeleccionado.value == e) null else e
    }

    // ====================== Inicio (indicadores) ======================
    val resumen: StateFlow<ResumenGeneral> = combine(
        repo.totalMuestras(),
        repo.conteoPorEstado(),
        repo.individuosDesde(Formatos.inicioDelDia()),
        repo.sinSincronizar()
    ) { total, porEstado, hoy, sinSync ->
        ResumenGeneral(
            totalMuestras = total,
            pendientes = porEstado.firstOrNull { it.estado == EstadoRevision.PENDIENTE }?.total ?: 0,
            individuosHoy = hoy,
            sinSincronizar = sinSync
        )
    }.stateIn(viewModelScope, compartir, ResumenGeneral())

    // ====================== Detalle / resumen de una muestra ======================
    private val _detalleId = MutableStateFlow<Long?>(null)

    val detalle: StateFlow<MuestraResumen?> = _detalleId
        .flatMapLatest { id -> if (id == null) flowOf<MuestraResumen?>(null) else repo.observarResumen(id) }
        .stateIn(viewModelScope, compartir, null)

    val adjuntosDetalle: StateFlow<List<Adjunto>> = _detalleId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList<Adjunto>()) else repo.adjuntos(id) }
        .stateIn(viewModelScope, compartir, emptyList())

    fun seleccionarMuestra(id: Long) {
        _detalleId.value = id
    }

    fun cambiarEstado(id: Long, estado: EstadoRevision) {
        viewModelScope.launch {
            repo.cambiarEstado(id, estado)
            messages.enviar("Estado actualizado: ${estado.etiqueta}")
        }
    }

    fun eliminarMuestra(id: Long, onListo: () -> Unit) {
        if (session.usuario.value?.rol?.puedeEliminar() != true) {
            messages.enviar("Tu rol no permite eliminar muestras")
            return
        }
        viewModelScope.launch {
            try {
                val adjuntos = repo.eliminarMuestra(id)
                adjuntos.forEach { storage.eliminar(it.ruta) }
                messages.enviar("Muestra #$id eliminada")
                onListo()
            } catch (e: Exception) {
                messages.enviar("No se pudo eliminar la muestra")
            }
        }
    }

    // ====================== Internet: sincronización (simulada) y exportación ======================
    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    /**
     * Simula el envío al servidor: el proyecto aún no tiene API. Requiere conexión a internet
     * y marca las muestras pendientes como sincronizadas.
     */
    fun sincronizar() {
        if (!online.value) {
            messages.enviar("Necesitas conexión a internet para sincronizar")
            return
        }
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            delay(1_500) // aquí iría la llamada a la API REST
            val n = repo.marcarSincronizadas()
            _sincronizando.value = false
            messages.enviar(if (n == 0) "No hay muestras pendientes de sincronizar" else "$n muestra(s) sincronizada(s) (simulado)")
        }
    }

    /** Exporta el historial filtrado a un CSV en la ubicación elegida con el selector de documentos. */
    fun exportarCsv(uri: Uri) {
        val filas = historial.value
        viewModelScope.launch {
            val ok = storage.escribir(uri, Reporte.csv(filas))
            messages.enviar(if (ok) "Reporte exportado (${filas.size} muestras)" else "No se pudo exportar el reporte")
        }
    }

    fun nombreArchivoReporte(): String = "historial_aquaxor_${Formatos.marcaArchivo()}.csv"
}
