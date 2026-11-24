package cl.aquaxor.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector
import cl.aquaxor.app.ui.components.colorEstado
import cl.aquaxor.app.ui.components.iconoEstado
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.RangoFecha
import cl.aquaxor.app.model.puedeRegistrar
import cl.aquaxor.app.ui.components.AppDrawerScaffold
import cl.aquaxor.app.ui.components.MuestraCard
import cl.aquaxor.app.ui.navigation.Ruta
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

/** Dashboard e historial: búsqueda, filtros, exportación a CSV y sincronización. */
@Composable
fun HistoryScreen(
    sessionVm: SessionViewModel,
    sampleVm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirMuestra: (Long) -> Unit
) {
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val online by sessionVm.online.collectAsStateWithLifecycle()
    val lista by sampleVm.historial.collectAsStateWithLifecycle()
    val filtros by sampleVm.filtros.collectAsStateWithLifecycle()
    val centros by sampleVm.centros.collectAsStateWithLifecycle()
    val sincronizando by sampleVm.sincronizando.collectAsStateWithLifecycle()
    val porEstado by sampleVm.conteoPorEstado.collectAsStateWithLifecycle()
    val porRango by sampleVm.conteoRangos.collectAsStateWithLifecycle()
    var menuCentro by remember { mutableStateOf(false) }

    // Documentos: el usuario elige dónde guardar el CSV (Storage Access Framework)
    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) sampleVm.exportarCsv(uri)
    }

    val hayFiltros = filtros.texto.isNotBlank() || filtros.estado != null ||
        filtros.rango != RangoFecha.TODOS || filtros.centroId != null

    AppDrawerScaffold(
        titulo = "Dashboard e historial",
        rutaActual = Ruta.Historial.ruta,
        usuario = usuario,
        online = online,
        snackbarHostState = snackbarHostState,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion,
        actions = {
            IconButton(onClick = { exportar.launch(sampleVm.nombreArchivoReporte()) }) {
                Icon(Icons.Default.FileDownload, contentDescription = "Exportar CSV")
            }
            IconButton(onClick = { sampleVm.sincronizar() }, enabled = !sincronizando) {
                if (sincronizando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                }
            }
        },
        floatingActionButton = {
            if (usuario?.rol?.puedeRegistrar() == true) {
                ExtendedFloatingActionButton(
                    onClick = { onNavegar(Ruta.Form.ruta) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva muestra") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = filtros.texto,
                onValueChange = sampleVm::onTextoBusqueda,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filtros.texto.isNotEmpty()) {
                        IconButton(onClick = { sampleVm.onTextoBusqueda("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                placeholder = { Text("Buscar por línea, centro, operador, tramo…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ---- Estado de revisión: color + ícono + cantidad ----
            EtiquetaFiltro(Icons.Default.FactCheck, "Estado de revisión")
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtros.estado == null,
                    onClick = { sampleVm.onFiltroEstado(null) },
                    label = { Text("Todos (${porEstado.values.sum()})") },
                    leadingIcon = { Icon(Icons.Default.AllInclusive, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                EstadoRevision.entries.forEach { e ->
                    val (fondo, texto) = colorEstado(e)
                    FilterChip(
                        selected = filtros.estado == e,
                        onClick = { sampleVm.onFiltroEstado(if (filtros.estado == e) null else e) },
                        label = { Text("${e.etiqueta} (${porEstado[e] ?: 0})") },
                        leadingIcon = { Icon(iconoEstado(e), contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = fondo.copy(alpha = 0.45f),
                            labelColor = texto,
                            iconColor = texto,
                            selectedContainerColor = fondo,
                            selectedLabelColor = texto,
                            selectedLeadingIconColor = texto
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filtros.estado == e,
                            borderColor = texto.copy(alpha = 0.35f),
                            selectedBorderColor = texto,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            // ---- Período (con la cantidad de muestras de cada uno) y centro ----
            EtiquetaFiltro(Icons.Default.DateRange, "Período y centro")
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RangoFecha.entries.forEach { r ->
                    FilterChip(
                        selected = filtros.rango == r,
                        onClick = { sampleVm.onFiltroRango(r) },
                        label = { Text("${r.etiqueta} (${porRango[r] ?: 0})") },
                        leadingIcon = { Icon(iconoRango(r), contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                Box {
                    FilterChip(
                        selected = filtros.centroId != null,
                        onClick = { menuCentro = true },
                        label = { Text(centros.firstOrNull { it.id == filtros.centroId }?.nombre ?: "Centro: todos") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(expanded = menuCentro, onDismissRequest = { menuCentro = false }) {
                        DropdownMenuItem(
                            text = { Text("Todos los centros") },
                            onClick = {
                                sampleVm.onFiltroCentro(null)
                                menuCentro = false
                            }
                        )
                        centros.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.nombre) },
                                onClick = {
                                    sampleVm.onFiltroCentro(c.id)
                                    menuCentro = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${lista.size} muestra(s) · ${lista.sumOf { it.muestra.cantidadIndividuos }} individuos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (hayFiltros) {
                    TextButton(onClick = { sampleVm.limpiarFiltros() }) { Text("Limpiar filtros") }
                }
            }

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (hayFiltros) "No hay muestras que coincidan con los filtros." else "Aún no hay muestras registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lista, key = { it.muestra.id }) { item ->
                        MuestraCard(item = item, onClick = { onAbrirMuestra(item.muestra.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EtiquetaFiltro(icono: ImageVector, texto: String) {
    Row(
        Modifier.padding(start = 16.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

private fun iconoRango(r: RangoFecha): ImageVector = when (r) {
    RangoFecha.TODOS -> Icons.Default.AllInclusive
    RangoFecha.HOY -> Icons.Default.Today
    RangoFecha.SIETE_DIAS -> Icons.Default.DateRange
    RangoFecha.TREINTA_DIAS -> Icons.Default.CalendarMonth
}
