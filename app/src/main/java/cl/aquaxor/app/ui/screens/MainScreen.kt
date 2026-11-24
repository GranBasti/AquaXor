package cl.aquaxor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.RangoFecha
import cl.aquaxor.app.model.esAdministrador
import cl.aquaxor.app.model.puedeRegistrar
import cl.aquaxor.app.ui.components.AnilloProgreso
import cl.aquaxor.app.ui.components.AppDrawerScaffold
import cl.aquaxor.app.ui.components.ClimaCard
import cl.aquaxor.app.ui.components.GraficoBarras
import cl.aquaxor.app.ui.navigation.Ruta
import cl.aquaxor.app.util.Estadisticas
import cl.aquaxor.app.viewmodel.ClimaViewModel
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sessionVm: SessionViewModel,
    sampleVm: SampleViewModel,
    climaVm: ClimaViewModel,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val online by sessionVm.online.collectAsStateWithLifecycle()
    val resumen by sampleVm.resumen.collectAsStateWithLifecycle()
    val semana by sampleVm.serieSemanal.collectAsStateWithLifecycle()
    val clima by climaVm.estado.collectAsStateWithLifecycle()
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Cabecera con degradado ----
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(Brush.linearGradient(listOf(Color(0xFF004B87), Color(0xFF1B7FBF))))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (usuario?.nombre?.firstOrNull() ?: 'A').uppercase(),
                            color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Hola, ${usuario?.nombre ?: ""}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(rol?.etiqueta ?: "", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(
                        if (online) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = if (online) "En línea" else "Sin conexión",
                        tint = Color.White
                    )
                }
            }

            // ---- Clima (Retrofit + Open-Meteo) ----
            ClimaCard(estado = clima, onActualizar = climaVm::actualizar)

            // ---- Indicadores (cada tarjeta se puede tocar para ver el detalle) ----
            Text("Toca un indicador para ver sus muestras", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard(
                    "Muestras registradas", "Ver historial completo", resumen.totalMuestras.toString(),
                    Icons.Default.Science, MaterialTheme.colorScheme.primary, Modifier.weight(1f)
                ) { sampleVm.limpiarFiltros(); onNavegar(Ruta.Historial.ruta) }
                KpiCard(
                    "Por revisar", "Estado «Pendiente»", resumen.pendientes.toString(),
                    Icons.Default.PendingActions, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)
                ) { sampleVm.mostrarEstado(EstadoRevision.PENDIENTE); onNavegar(Ruta.Estados.ruta) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard(
                    "Individuos hoy", "Muestras de hoy", resumen.individuosHoy.toString(),
                    Icons.Default.Today, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)
                ) { sampleVm.limpiarFiltros(); sampleVm.onFiltroRango(RangoFecha.HOY); onNavegar(Ruta.Historial.ruta) }
                KpiCard(
                    "Sin enviar a la nube", "Toca para sincronizar", resumen.sinSincronizar.toString(),
                    Icons.Default.CloudOff, Color(0xFF6B7A89), Modifier.weight(1f)
                ) { sampleVm.sincronizar() }
            }

            // ---- Gráfico semanal + anillo de sincronización ----
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Individuos contados · últimos 7 días", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    GraficoBarras(semana)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AnilloProgreso(Estadisticas.fraccion(resumen.totalMuestras - resumen.sinSincronizar, resumen.totalMuestras))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Respaldo en InsForge", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${Estadisticas.porcentajeSincronizado(resumen.totalMuestras, resumen.sinSincronizar)}% de las muestras sincronizadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- Accesos (cuadrícula con íconos) ----
            val accesos = buildList {
                if (rol?.puedeRegistrar() == true) add(Acceso("Registrar", "Conteo y evidencia", Icons.Default.EditNote, Ruta.Form.ruta, Color(0xFFF26922)))
                add(Acceso("Historial", "Busca y exporta", Icons.Default.Dashboard, Ruta.Historial.ruta, Color(0xFF004B87)))
                add(Acceso("Estados", "Etapas de revisión", Icons.Default.FactCheck, Ruta.Estados.ruta, Color(0xFF27AE60)))
                if (rol?.esAdministrador() == true) add(Acceso("Administración", "Usuarios y centros", Icons.Default.AdminPanelSettings, Ruta.Admin.ruta, Color(0xFF4A5A6A)))
            }
            accesos.chunked(2).forEach { fila ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fila.forEach { a -> AccesoCard(a, { onNavegar(a.ruta) }, Modifier.weight(1f)) }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(72.dp)) // espacio para que el FAB no tape el último acceso
        }
    }
}

private data class Acceso(val titulo: String, val subtitulo: String, val icono: ImageVector, val ruta: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KpiCard(
    titulo: String,
    ayuda: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.weight(1f))
                Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ayuda, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccesoCard(a: Acceso, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = a.color),
        modifier = modifier.height(110.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(a.icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Column {
                Text(a.titulo, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(a.subtitulo, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
