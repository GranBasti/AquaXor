package cl.aquaxor.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.TipoAdjunto
import cl.aquaxor.app.ui.components.AquaTopBar
import cl.aquaxor.app.ui.components.FilaDocumento
import cl.aquaxor.app.ui.components.MiniaturaFoto
import cl.aquaxor.app.ui.components.abrirArchivo
import cl.aquaxor.app.util.Formatos
import cl.aquaxor.app.viewmodel.SampleViewModel

private val TIPOS_DOCUMENTO = arrayOf(
    "application/pdf",
    "text/plain",
    "text/csv",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
)

/** Paso 2: evidencia con cámara, galería y documentos; al final se guarda en Room. */
@Composable
fun EvidenciaScreen(
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

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        vm.onFotoResultado(exito)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) camara.launch(vm.prepararFoto()) else vm.avisar("Permiso de cámara denegado")
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.importar(uri, TipoAdjunto.FOTO)
    }
    val documento = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importar(uri, TipoAdjunto.DOCUMENTO)
    }

    fun tomarFoto() {
        val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (permitido) camara.launch(vm.prepararFoto()) else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    fun abrir(ruta: String) {
        abrirArchivo(context, vm.uriAdjunto(ruta), vm.mimeAdjunto(ruta)) {
            vm.avisar("No hay una aplicación para abrir este archivo")
        }
    }

    val fotos = b.adjuntos.filter { it.tipo == TipoAdjunto.FOTO }
    val documentos = b.adjuntos.filter { it.tipo == TipoAdjunto.DOCUMENTO }

    Scaffold(
        topBar = { AquaTopBar("Evidencia de la muestra", onBack) },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text(
                        "${fotos.size} foto(s) · ${documentos.size} documento(s)",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { vm.guardar(onGuardada) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        icon = { Icon(Icons.Default.Save, contentDescription = null) },
                        text = { Text(if (b.editando) "ACTUALIZAR" else "GUARDAR") }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Paso 2 de 2 · Resumen", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${b.cantidad} individuos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            listOf(
                                centros.firstOrNull { it.id == b.centroId }?.nombre,
                                trenes.firstOrNull { it.id == b.trenId }?.nombre,
                                lineas.firstOrNull { it.id == b.lineaId }?.nombre
                            ).filterNotNull().joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${Formatos.fechaHora(b.fechaHora)} · Tramo ${b.tramo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { tomarFoto() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cámara")
                    }
                    FilledTonalButton(
                        onClick = { galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Galería")
                    }
                    FilledTonalButton(onClick = { documento.launch(TIPOS_DOCUMENTO) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Archivo")
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Fotografías (${fotos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (fotos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Toma una foto o elige una imagen de la galería para validar el conteo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(fotos, key = { it.ruta }) { foto ->
                MiniaturaFoto(
                    ruta = foto.ruta,
                    onClick = { abrir(foto.ruta) },
                    onQuitar = { vm.quitarAdjunto(foto) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Documentos (${documentos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (documentos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Opcional: adjunta un PDF, planilla u otro documento de respaldo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(documentos, key = { it.ruta }, span = { GridItemSpan(maxLineSpan) }) { doc ->
                FilaDocumento(
                    nombre = doc.nombre,
                    onClick = { abrir(doc.ruta) },
                    onQuitar = { vm.quitarAdjunto(doc) }
                )
            }
        }
    }
}
