package cl.aquaxor.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.aquaxor.app.model.Usuario
import cl.aquaxor.app.model.esAdministrador
import cl.aquaxor.app.model.puedeRegistrar
import cl.aquaxor.app.ui.navigation.Ruta
import kotlinx.coroutines.launch

private data class ItemMenu(val titulo: String, val ruta: String, val icono: ImageVector)

/**
 * Estructura común de las pantallas principales: ModalNavigationDrawer + Scaffold
 * con TopAppBar (menú hamburguesa), SnackbarHost y FAB opcional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScaffold(
    titulo: String,
    rutaActual: String,
    usuario: Usuario?,
    online: Boolean,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val rol = usuario?.rol

    val items = buildList {
        add(ItemMenu("Inicio", Ruta.Main.ruta, Icons.Default.Home))
        if (rol?.puedeRegistrar() == true) add(ItemMenu("Nueva muestra", Ruta.Form.ruta, Icons.Default.EditNote))
        add(ItemMenu("Dashboard e historial", Ruta.Historial.ruta, Icons.Default.Dashboard))
        add(ItemMenu("Estados de muestras", Ruta.Estados.ruta, Icons.Default.FactCheck))
        if (rol?.esAdministrador() == true) add(ItemMenu("Administración", Ruta.Admin.ruta, Icons.Default.AdminPanelSettings))
    }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {
                    Icon(Icons.Default.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("AquaXor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text(usuario?.nombre ?: "", color = MaterialTheme.colorScheme.onPrimary)
                    Text(rol?.etiqueta ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (online) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (online) "En línea" else "Sin conexión",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.titulo) },
                        icon = { Icon(item.icono, contentDescription = null) },
                        selected = item.ruta == rutaActual,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.ruta != rutaActual || item.ruta == Ruta.Form.ruta) onNavegar(item.ruta)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titulo) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = actions,
                    colors = aquaTopBarColors()
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}
