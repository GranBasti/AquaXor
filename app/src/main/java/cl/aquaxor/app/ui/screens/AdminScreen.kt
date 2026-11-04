package cl.aquaxor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Rol
import cl.aquaxor.app.model.Tren
import cl.aquaxor.app.model.Usuario
import cl.aquaxor.app.ui.components.AppDrawerScaffold
import cl.aquaxor.app.ui.components.CampoDropdown
import cl.aquaxor.app.ui.components.DialogoConfirmacion
import cl.aquaxor.app.ui.navigation.Ruta
import cl.aquaxor.app.viewmodel.AdminViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel

/** Administración (CRUD) de centros, trenes, líneas y usuarios. Solo rol Administrador. */
@Composable
fun AdminScreen(
    sessionVm: SessionViewModel,
    adminVm: AdminViewModel,
    snackbarHostState: SnackbarHostState,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by sessionVm.usuario.collectAsStateWithLifecycle()
    val online by sessionVm.online.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val titulos = listOf("Centros", "Trenes", "Líneas", "Usuarios")

    AppDrawerScaffold(
        titulo = "Administración",
        rutaActual = Ruta.Admin.ruta,
        usuario = usuario,
        online = online,
        snackbarHostState = snackbarHostState,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                titulos.forEachIndexed { indice, titulo ->
                    Tab(selected = tab == indice, onClick = { tab = indice }, text = { Text(titulo) })
                }
            }
            when (tab) {
                0 -> TabCentros(adminVm)
                1 -> TabTrenes(adminVm)
                2 -> TabLineas(adminVm)
                else -> TabUsuarios(adminVm)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Componentes genéricos
// ---------------------------------------------------------------------------

@Composable
private fun <T> ListaAdmin(
    lista: List<T>,
    clave: (T) -> Long,
    titulo: (T) -> String,
    subtitulo: (T) -> String,
    textoVacio: String,
    onEditar: (T) -> Unit,
    onEliminar: (T) -> Unit,
    onNuevo: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (lista.isEmpty()) {
            Text(
                textoVacio,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lista, key = clave) { elemento ->
                val sub = subtitulo(elemento)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    ListItem(
                        headlineContent = { Text(titulo(elemento)) },
                        supportingContent = { if (sub.isNotBlank()) Text(sub) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { onEditar(elemento) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { onEliminar(elemento) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onNuevo,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar")
        }
    }
}

@Composable
private fun DialogoFormulario(
    titulo: String,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                Arrangement.spacedBy(10.dp),
                content = contenido
            )
        },
        confirmButton = { TextButton(onClick = onGuardar) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

// ---------------------------------------------------------------------------
// Centros
// ---------------------------------------------------------------------------

@Composable
private fun TabCentros(vm: AdminViewModel) {
    val centros by vm.centros.collectAsStateWithLifecycle()
    var creando by rememberSaveable { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Centro?>(null) }
    var eliminando by remember { mutableStateOf<Centro?>(null) }

    ListaAdmin(
        lista = centros,
        clave = { it.id },
        titulo = { it.nombre },
        subtitulo = { "" },
        textoVacio = "No hay centros registrados.",
        onEditar = { editando = it },
        onEliminar = { eliminando = it },
        onNuevo = { creando = true }
    )

    if (creando || editando != null) {
        var nombre by remember(editando) { mutableStateOf(editando?.nombre ?: "") }
        DialogoFormulario(
            titulo = if (editando == null) "Nuevo centro" else "Editar centro",
            onGuardar = {
                vm.guardarCentro(editando?.id, nombre)
                creando = false
                editando = null
            },
            onCancelar = {
                creando = false
                editando = null
            }
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del centro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    eliminando?.let { c ->
        DialogoConfirmacion(
            titulo = "Eliminar centro",
            mensaje = "¿Eliminar \"${c.nombre}\"? Solo es posible si no tiene trenes ni muestras asociadas.",
            onConfirmar = {
                vm.eliminarCentro(c)
                eliminando = null
            },
            onCancelar = { eliminando = null }
        )
    }
}

// ---------------------------------------------------------------------------
// Trenes
// ---------------------------------------------------------------------------

@Composable
private fun TabTrenes(vm: AdminViewModel) {
    val centros by vm.centros.collectAsStateWithLifecycle()
    val trenes by vm.trenes.collectAsStateWithLifecycle()
    var creando by rememberSaveable { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Tren?>(null) }
    var eliminando by remember { mutableStateOf<Tren?>(null) }

    ListaAdmin(
        lista = trenes,
        clave = { it.id },
        titulo = { it.nombre },
        subtitulo = { t -> centros.firstOrNull { it.id == t.centroId }?.nombre.orEmpty() },
        textoVacio = "No hay trenes registrados.",
        onEditar = { editando = it },
        onEliminar = { eliminando = it },
        onNuevo = { creando = true }
    )

    if (creando || editando != null) {
        var nombre by remember(editando) { mutableStateOf(editando?.nombre ?: "") }
        var centroId by remember(editando) { mutableStateOf(editando?.centroId) }
        DialogoFormulario(
            titulo = if (editando == null) "Nuevo tren" else "Editar tren",
            onGuardar = {
                vm.guardarTren(editando?.id, centroId, nombre)
                creando = false
                editando = null
            },
            onCancelar = {
                creando = false
                editando = null
            }
        ) {
            CampoDropdown(
                label = "Centro",
                textoSeleccion = centros.firstOrNull { it.id == centroId }?.nombre.orEmpty(),
                opciones = centros,
                textoOpcion = { it.nombre },
                onSeleccion = { centroId = it.id },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del tren") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    eliminando?.let { t ->
        DialogoConfirmacion(
            titulo = "Eliminar tren",
            mensaje = "¿Eliminar \"${t.nombre}\"? Solo es posible si no tiene líneas ni muestras asociadas.",
            onConfirmar = {
                vm.eliminarTren(t)
                eliminando = null
            },
            onCancelar = { eliminando = null }
        )
    }
}

// ---------------------------------------------------------------------------
// Líneas
// ---------------------------------------------------------------------------

@Composable
private fun TabLineas(vm: AdminViewModel) {
    val centros by vm.centros.collectAsStateWithLifecycle()
    val trenes by vm.trenes.collectAsStateWithLifecycle()
    val lineas by vm.lineas.collectAsStateWithLifecycle()
    var creando by rememberSaveable { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Linea?>(null) }
    var eliminando by remember { mutableStateOf<Linea?>(null) }

    fun etiquetaTren(t: Tren): String =
        "${centros.firstOrNull { it.id == t.centroId }?.nombre ?: "?"} · ${t.nombre}"

    ListaAdmin(
        lista = lineas,
        clave = { it.id },
        titulo = { it.nombre },
        subtitulo = { l -> trenes.firstOrNull { it.id == l.trenId }?.let { etiquetaTren(it) }.orEmpty() },
        textoVacio = "No hay líneas registradas.",
        onEditar = { editando = it },
        onEliminar = { eliminando = it },
        onNuevo = { creando = true }
    )

    if (creando || editando != null) {
        var nombre by remember(editando) { mutableStateOf(editando?.nombre ?: "") }
        var trenId by remember(editando) { mutableStateOf(editando?.trenId) }
        DialogoFormulario(
            titulo = if (editando == null) "Nueva línea" else "Editar línea",
            onGuardar = {
                vm.guardarLinea(editando?.id, trenId, nombre)
                creando = false
                editando = null
            },
            onCancelar = {
                creando = false
                editando = null
            }
        ) {
            CampoDropdown(
                label = "Tren",
                textoSeleccion = trenes.firstOrNull { it.id == trenId }?.let { etiquetaTren(it) }.orEmpty(),
                opciones = trenes,
                textoOpcion = { etiquetaTren(it) },
                onSeleccion = { trenId = it.id },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la línea") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    eliminando?.let { l ->
        DialogoConfirmacion(
            titulo = "Eliminar línea",
            mensaje = "¿Eliminar \"${l.nombre}\"? Solo es posible si no tiene muestras asociadas.",
            onConfirmar = {
                vm.eliminarLinea(l)
                eliminando = null
            },
            onCancelar = { eliminando = null }
        )
    }
}

// ---------------------------------------------------------------------------
// Usuarios
// ---------------------------------------------------------------------------

@Composable
private fun TabUsuarios(vm: AdminViewModel) {
    val usuarios by vm.usuarios.collectAsStateWithLifecycle()
    var creando by rememberSaveable { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Usuario?>(null) }
    var eliminando by remember { mutableStateOf<Usuario?>(null) }

    ListaAdmin(
        lista = usuarios,
        clave = { it.id },
        titulo = { it.nombre },
        subtitulo = { "RUT ${it.rut} · ${it.rol.etiqueta}" + if (it.activo) "" else " · Inactivo" },
        textoVacio = "No hay usuarios registrados.",
        onEditar = { editando = it },
        onEliminar = { eliminando = it },
        onNuevo = { creando = true }
    )

    if (creando || editando != null) {
        val actual = editando
        var rut by remember(actual) { mutableStateOf(actual?.rut ?: "") }
        var nombre by remember(actual) { mutableStateOf(actual?.nombre ?: "") }
        var rol by remember(actual) { mutableStateOf(actual?.rol ?: Rol.OPERADOR) }
        var password by remember(actual) { mutableStateOf("") }
        var activo by remember(actual) { mutableStateOf(actual?.activo ?: true) }
        DialogoFormulario(
            titulo = if (actual == null) "Nuevo usuario" else "Editar usuario",
            onGuardar = {
                vm.guardarUsuario(actual, rut, nombre, rol, password, activo)
                creando = false
                editando = null
            },
            onCancelar = {
                creando = false
                editando = null
            }
        ) {
            OutlinedTextField(
                value = rut,
                onValueChange = { rut = it },
                label = { Text("RUT (sin puntos ni guion)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            CampoDropdown(
                label = "Rol",
                textoSeleccion = rol.etiqueta,
                opciones = Rol.entries,
                textoOpcion = { it.etiqueta },
                onSeleccion = { rol = it },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (actual == null) "Contraseña" else "Contraseña (vacío = no cambiar)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Usuario activo", modifier = Modifier.weight(1f))
                Switch(checked = activo, onCheckedChange = { activo = it })
            }
        }
    }
    eliminando?.let { u ->
        DialogoConfirmacion(
            titulo = "Eliminar usuario",
            mensaje = "¿Eliminar a \"${u.nombre}\"? Si ya registró muestras no se podrá eliminar; desactívalo en su lugar.",
            onConfirmar = {
                vm.eliminarUsuario(u)
                eliminando = null
            },
            onCancelar = { eliminando = null }
        )
    }
}
