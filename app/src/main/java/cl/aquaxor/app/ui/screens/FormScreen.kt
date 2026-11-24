package cl.aquaxor.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.TipoAdjunto
import cl.aquaxor.app.ui.components.AquaTopBar
import cl.aquaxor.app.ui.components.CampoCompacto
import cl.aquaxor.app.ui.components.DropdownCompacto
import cl.aquaxor.app.ui.components.MiniaturaFoto
import cl.aquaxor.app.ui.components.abrirArchivo
import cl.aquaxor.app.util.Formatos
import cl.aquaxor.app.viewmodel.SampleViewModel
import kotlinx.coroutines.delay

private val TIPOS_DOCUMENTO = arrayOf(
    "application/pdf",
    "text/plain",
    "text/csv",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
)

@OptIn(ExperimentalLayoutApi::class)
/**
 * Formulario de muestra en UNA sola pantalla: datos, conteo y evidencia (cámara, galería, archivos).
 * La fecha y hora no se editan: en una muestra nueva se muestra el reloj en tiempo real.
 */
@Composable
fun FormScreen(
    vm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onGuardada: (Long) -> Unit
) {
    val b by vm.borrador.collectAsStateWithLifecycle()
    val centros by vm.centros.collectAsStateWithLifecycle()
    val trenes by vm.trenes.collectAsStateWithLifecycle()
    val lineas by vm.lineas.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Reloj en tiempo real (solo muestras nuevas)
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            ahora = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val momento = if (b.editando) b.fechaHora else ahora

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { vm.onFotoResultado(it) }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) camara.launch(vm.prepararFoto()) else vm.avisar("Permiso de cámara denegado")
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.importar(uri, TipoAdjunto.FOTO)
    }
    val documento = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importar(uri, TipoAdjunto.DOCUMENTO)
    }

    fun tomarFoto() {
        val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (permitido) camara.launch(vm.prepararFoto()) else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    fun abrir(ruta: String) {
        abrirArchivo(context, vm.uriAdjunto(ruta), vm.mimeAdjunto(ruta)) {
            vm.avisar("No hay una aplicación para abrir este archivo")
        }
    }

    val fotos = b.adjuntos.filter { it.tipo == TipoAdjunto.FOTO }
    val documentos = b.adjuntos.filter { it.tipo == TipoAdjunto.DOCUMENTO }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            AquaTopBar(
                titulo = if (b.editando) "Editar muestra #${b.original?.id}" else "Nueva muestra",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // Sin scroll: todos los bloques se reparten el alto de la pantalla (weight) y crecen para llenarla.
        // Con el teclado abierto se ocultan reloj y evidencia para que los campos sigan cómodos.
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).imePadding()) {
        val tecladoAbierto = WindowInsets.isImeVisible
        val pantallaChica = maxHeight < 600.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---- Fecha y hora (solo lectura, tiempo real) ----
            if (!tecladoAbierto) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().weight(0.8f)
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(Formatos.fecha(momento), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(Formatos.horaCompleta(momento), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ---- Centro / Tren / Línea ----
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownCompacto(
                    etiqueta = "Centro", icono = Icons.Default.LocationOn,
                    textoSeleccion = centros.firstOrNull { it.id == b.centroId }?.nombre.orEmpty(),
                    opciones = centros, textoOpcion = { it.nombre }, onSeleccion = { vm.onCentro(it.id) },
                    error = b.errores["centro"], llenar = true, modifier = Modifier.weight(1.5f).fillMaxHeight()
                )
                DropdownCompacto(
                    etiqueta = "Tren", icono = Icons.Default.Hub,
                    textoSeleccion = trenes.firstOrNull { it.id == b.trenId }?.nombre.orEmpty(),
                    opciones = trenes, textoOpcion = { it.nombre }, onSeleccion = { vm.onTren(it.id) },
                    enabled = b.centroId != null, error = b.errores["tren"], llenar = true,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                DropdownCompacto(
                    etiqueta = "Línea", icono = Icons.Default.Timeline,
                    textoSeleccion = lineas.firstOrNull { it.id == b.lineaId }?.nombre.orEmpty(),
                    opciones = lineas, textoOpcion = { it.nombre }, onSeleccion = { vm.onLinea(it.id) },
                    enabled = b.trenId != null, error = b.errores["linea"], llenar = true,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // ---- Tramo / Profundidad / Talla ----
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoCompacto(
                    etiqueta = "Tramo", icono = Icons.Default.LinearScale, valor = b.tramo, onCambio = vm::onTramo,
                    placeholder = "T1", error = b.errores["tramo"], llenar = true, modifier = Modifier.weight(1f).fillMaxHeight()
                )
                CampoCompacto(
                    etiqueta = "Profund. (m)", icono = Icons.Default.Height, valor = b.profundidad, onCambio = vm::onProfundidad,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    error = b.errores["profundidad"], llenar = true, modifier = Modifier.weight(1f).fillMaxHeight()
                )
                CampoCompacto(
                    etiqueta = "Talla (mm)", icono = Icons.Default.Straighten, valor = b.talla, onCambio = vm::onTalla,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    error = b.errores["talla"], llenar = true, modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // ---- Conteo ----
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().weight(if (tecladoAbierto || pantallaChica) 1.2f else 1.7f)
            ) {
                Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("Conteo de individuos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        b.errores["cantidad"]?.let {
                            Spacer(Modifier.width(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(onClick = { vm.sumarCantidad(-1) }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar uno")
                        }
                        CampoCompacto(
                            etiqueta = "", icono = Icons.Default.Waves, valor = b.cantidad.toString(),
                            onCambio = { t -> vm.onCantidad(t.filter { it.isDigit() }.toIntOrNull() ?: 0) },
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Number),
                            error = b.errores["cantidad"], centrado = true, llenar = true, tamanoTexto = 26.sp,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        FilledIconButton(onClick = { vm.sumarCantidad(1) }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar uno")
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 50).forEach { n ->
                            AssistChip(onClick = { vm.sumarCantidad(n) }, label = { Text("+$n") }, modifier = Modifier.weight(1f))
                        }
                        AssistChip(onClick = { vm.onCantidad(0) }, label = { Text("Cero") }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // ---- Observaciones ----
            CampoCompacto(
                etiqueta = "Observaciones", icono = Icons.Default.Edit, valor = b.observaciones, onCambio = vm::onObservaciones,
                placeholder = "Opcional", unaLinea = false, llenar = true,
                modifier = Modifier.fillMaxWidth().weight(if (tecladoAbierto) 1.6f else 1.1f)
            )

            // ---- Evidencia (se oculta con el teclado abierto) ----
            if (!tecladoAbierto) {
                Row(Modifier.fillMaxWidth().weight(0.8f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonEvidencia("Cámara", Icons.Default.PhotoCamera, Modifier.weight(1f).fillMaxHeight()) { tomarFoto() }
                    BotonEvidencia("Galería", Icons.Default.PhotoLibrary, Modifier.weight(1f).fillMaxHeight()) {
                        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    BotonEvidencia("Archivo", Icons.Default.AttachFile, Modifier.weight(1f).fillMaxHeight()) { documento.launch(TIPOS_DOCUMENTO) }
                }
                if (!pantallaChica) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (b.adjuntos.isEmpty()) {
                            Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Las fotos y documentos aparecerán aquí (opcional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyRow(
                                Modifier.fillMaxSize().padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(fotos, key = { it.ruta }) { foto ->
                                    MiniaturaFoto(
                                        ruta = foto.ruta, onClick = { abrir(foto.ruta) },
                                        onQuitar = { vm.quitarAdjunto(foto) }, modifier = Modifier.fillMaxHeight()
                                    )
                                }
                                items(documentos, key = { it.ruta }) { doc ->
                                    InputChip(
                                        selected = false,
                                        onClick = { abrir(doc.ruta) },
                                        label = { Text(doc.nombre, maxLines = 1, modifier = Modifier.width(110.dp)) },
                                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        trailingIcon = {
                                            IconButton(onClick = { vm.quitarAdjunto(doc) }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Quitar documento", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (b.adjuntos.isNotEmpty()) {
                    Text(
                        "${fotos.size} foto(s) · ${documentos.size} documento(s) adjuntos",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = { vm.guardar(onGuardada) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (b.editando) "ACTUALIZAR MUESTRA" else "GUARDAR MUESTRA", style = MaterialTheme.typography.titleSmall)
            }
        }
        }
    }
}

@Composable
private fun BotonEvidencia(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(26.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}
