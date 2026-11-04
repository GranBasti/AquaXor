package cl.aquaxor.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.TipoAdjunto
import cl.aquaxor.app.model.estadosPermitidos
import cl.aquaxor.app.model.puedeEliminar
import cl.aquaxor.app.ui.components.AquaTopBar
import cl.aquaxor.app.ui.components.DialogoConfirmacion
import cl.aquaxor.app.ui.components.EstadoChip
import cl.aquaxor.app.ui.components.FilaDocumento
import cl.aquaxor.app.ui.components.MiniaturaFoto
import cl.aquaxor.app.ui.components.abrirArchivo
import cl.aquaxor.app.util.Formatos
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

/** Resumen de una muestra: datos, estado de revisión, fotos y documentos. */
@Composable
fun DetalleScreen(
    id: Long,
    sessionVm: SessionViewModel,
    sampleVm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onEliminada: () -> Unit
) {
    LaunchedEffect(id) { sampleVm.seleccionarMuestra(id) }

    val detalle by sampleVm.detalle.collectAsStateWithLifecycle()
    val adjuntos by sampleVm.adjuntosDetalle.collectAsStateWithLifecycle()
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val rol = usuario?.rol
    val context = LocalContext.current
    var dialogoEstado by rememberSaveable { mutableStateOf(false) }
    var dialogoEliminar by rememberSaveable { mutableStateOf(false) }

    fun abrir(ruta: String) {
        abrirArchivo(context, sampleVm.uriAdjunto(ruta), sampleVm.mimeAdjunto(ruta)) {
            sampleVm.avisar("No hay una aplicación para abrir este archivo")
        }
    }

    Scaffold(
        topBar = {
            AquaTopBar(
                titulo = "Muestra #$id",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                    if (rol?.puedeEliminar() == true) {
                        IconButton(onClick = { dialogoEliminar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val item = detalle?.takeIf { it.muestra.id == id }
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val m = item.muestra
            val fotos = adjuntos.filter { it.tipo == TipoAdjunto.FOTO }
            val documentos = adjuntos.filter { it.tipo == TipoAdjunto.DOCUMENTO }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    m.cantidadIndividuos.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("individuos contados", style = MaterialTheme.typography.bodyMedium)
                            }
                            EstadoChip(m.estado)
                        }
                        Button(onClick = { dialogoEstado = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("CAMBIAR ESTADO DE REVISIÓN")
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilaDato("Fecha y hora", Formatos.fechaHora(m.fechaHora))
                        FilaDato("Centro", item.centroNombre)
                        FilaDato("Tren", item.trenNombre)
                        FilaDato("Línea", item.lineaNombre)
                        FilaDato("Tramo", m.tramo)
                        FilaDato("Profundidad", m.profundidadM?.let { "${Formatos.decimal(it)} m" } ?: "—")
                        FilaDato("Talla promedio", m.tallaMm?.let { "${Formatos.decimal(it)} mm" } ?: "—")
                        FilaDato("Registrada por", item.operadorNombre)
                        FilaDato("Sincronización", if (m.sincronizada) "Sincronizada" else "Pendiente")
                    }
                }

                if (m.observaciones.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Observaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(m.observaciones)
                        }
                    }
                }

                Text("Fotografías (${fotos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (fotos.isEmpty()) {
                    Text("Sin fotografías.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(fotos, key = { it.id }) { foto ->
                            MiniaturaFoto(ruta = foto.ruta, onClick = { abrir(foto.ruta) }, modifier = Modifier.size(120.dp))
                        }
                    }
                }

                Text("Documentos (${documentos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (documentos.isEmpty()) {
                    Text("Sin documentos adjuntos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    documentos.forEach { doc ->
                        FilaDocumento(nombre = doc.nombre, onClick = { abrir(doc.ruta) })
                    }
                }
            }

            if (dialogoEstado) {
                val permitidos = rol?.estadosPermitidos().orEmpty()
                AlertDialog(
                    onDismissRequest = { dialogoEstado = false },
                    title = { Text("Estado de revisión") },
                    text = {
                        Column {
                            permitidos.forEach { estado ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            sampleVm.cambiarEstado(id, estado)
                                            dialogoEstado = false
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = estado == m.estado, onClick = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text(estado.etiqueta)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = { TextButton(onClick = { dialogoEstado = false }) { Text("Cancelar") } }
                )
            }
        }
    }

    if (dialogoEliminar) {
        DialogoConfirmacion(
            titulo = "Eliminar muestra",
            mensaje = "Se eliminará la muestra #$id con sus fotos y documentos. Esta acción no se puede deshacer.",
            onConfirmar = {
                dialogoEliminar = false
                sampleVm.eliminarMuestra(id, onEliminada)
            },
            onCancelar = { dialogoEliminar = false }
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}
