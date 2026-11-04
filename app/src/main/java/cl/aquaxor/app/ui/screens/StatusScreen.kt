package cl.aquaxor.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.ui.components.AppDrawerScaffold
import cl.aquaxor.app.ui.components.MuestraCard
import cl.aquaxor.app.ui.components.colorEstado
import cl.aquaxor.app.ui.navigation.Ruta
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

/** Estados de muestra: resumen por etapa de revisión y listado filtrable. */
@Composable
fun StatusScreen(
    sessionVm: SessionViewModel,
    sampleVm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirMuestra: (Long) -> Unit
) {
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val online by sessionVm.online.collectAsStateWithLifecycle()
    val conteo by sampleVm.conteoPorEstado.collectAsStateWithLifecycle()
    val seleccionado by sampleVm.estadoSeleccionado.collectAsStateWithLifecycle()
    val lista by sampleVm.muestrasPorEstado.collectAsStateWithLifecycle()

    AppDrawerScaffold(
        titulo = "Estados de muestras",
        rutaActual = Ruta.Estados.ruta,
        usuario = usuario,
        online = online,
        snackbarHostState = snackbarHostState,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Toca un estado para filtrar la lista",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EstadoRevision.entries.chunked(2).forEach { fila ->
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        fila.forEach { estado ->
                            EstadoCard(
                                estado = estado,
                                total = conteo[estado] ?: 0,
                                seleccionado = seleccionado == estado,
                                onClick = { sampleVm.seleccionarEstado(estado) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            if (lista.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay muestras en este estado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(lista, key = { it.muestra.id }) { item ->
                MuestraCard(item = item, onClick = { onAbrirMuestra(item.muestra.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoCard(
    estado: EstadoRevision,
    total: Int,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (fondo, texto) = colorEstado(estado)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = if (seleccionado) BorderStroke(2.dp, texto) else null,
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(total.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = texto)
            Text(estado.etiqueta, style = MaterialTheme.typography.labelLarge, color = texto)
        }
    }
}
