package cl.aquaxor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.esAdministrador
import cl.aquaxor.app.model.puedeRegistrar
import cl.aquaxor.app.ui.components.AppDrawerScaffold
import cl.aquaxor.app.ui.navigation.Ruta
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sessionVm: SessionViewModel,
    sampleVm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val online by sessionVm.online.collectAsStateWithLifecycle()
    val resumen by sampleVm.resumen.collectAsStateWithLifecycle()
    val rol = usuario?.rol

    AppDrawerScaffold(
        titulo = "AquaXor",
        rutaActual = Ruta.Main.ruta,
        usuario = usuario,
        online = online,
        snackbarHostState = snackbarHostState,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion,
        floatingActionButton = {
            if (rol?.puedeRegistrar() == true) {
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
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Hola, ${usuario?.nombre ?: ""}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(rol?.etiqueta ?: "", style = MaterialTheme.typography.bodyMedium)
                    AssistChip(
                        onClick = {},
                        label = { Text(if (online) "En línea" else "Sin conexión (modo local)") },
                        leadingIcon = {
                            Icon(
                                if (online) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Muestras", resumen.totalMuestras.toString(), Modifier.weight(1f))
                KpiCard("Pendientes", resumen.pendientes.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Individuos hoy", resumen.individuosHoy.toString(), Modifier.weight(1f))
                KpiCard("Sin sincronizar", resumen.sinSincronizar.toString(), Modifier.weight(1f))
            }

            if (rol?.puedeRegistrar() == true) {
                MenuCard(
                    "REGISTRAR MUESTRA", "Datos, conteo y evidencia",
                    Icons.Default.EditNote, MaterialTheme.colorScheme.secondary
                ) { onNavegar(Ruta.Form.ruta) }
            }
            MenuCard(
                "DASHBOARD E HISTORIAL", "Busca, filtra y exporta",
                Icons.Default.Dashboard, MaterialTheme.colorScheme.primary
            ) { onNavegar(Ruta.Historial.ruta) }
            MenuCard(
                "ESTADOS DE MUESTRAS", "Etapa de revisión de cada muestra",
                Icons.Default.FactCheck, MaterialTheme.colorScheme.tertiary
            ) { onNavegar(Ruta.Estados.ruta) }
            if (rol?.esAdministrador() == true) {
                MenuCard(
                    "ADMINISTRACIÓN", "Usuarios, centros, trenes y líneas",
                    Icons.Default.AdminPanelSettings, Color(0xFF4A5A6A)
                ) { onNavegar(Ruta.Admin.ruta) }
            }
            Spacer(Modifier.height(72.dp)) // espacio para que el FAB no tape el último botón
        }
    }
}

@Composable
private fun KpiCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuCard(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitulo, color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
