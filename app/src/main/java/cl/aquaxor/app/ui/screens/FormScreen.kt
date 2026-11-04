package cl.aquaxor.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.aquaxor.app.ui.components.AquaTopBar
import cl.aquaxor.app.ui.components.CampoDropdown
import cl.aquaxor.app.ui.components.SelectorFecha
import cl.aquaxor.app.ui.components.SelectorHora
import cl.aquaxor.app.util.Formatos
import cl.aquaxor.app.viewmodel.SampleViewModel

/** Paso 1: datos de la muestra (centro, tren, línea, fecha/hora, tramo) y conteo de individuos. */
@Composable
fun FormScreen(
    vm: SampleViewModel,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSiguiente: () -> Unit
) {
    val b by vm.borrador.collectAsStateWithLifecycle()
    val centros by vm.centros.collectAsStateWithLifecycle()
    val trenes by vm.trenes.collectAsStateWithLifecycle()
    val lineas by vm.lineas.collectAsStateWithLifecycle()
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var mostrarHora by rememberSaveable { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Paso 1 de 2 · Datos y conteo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Fecha y hora del muestreo (DatePicker + TimePicker de Material 3)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { mostrarFecha = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Formatos.fecha(b.fechaHora))
                }
                OutlinedButton(onClick = { mostrarHora = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Formatos.hora(b.fechaHora))
                }
            }

            CampoDropdown(
                label = "Centro",
                textoSeleccion = centros.firstOrNull { it.id == b.centroId }?.nombre.orEmpty(),
                opciones = centros,
                textoOpcion = { it.nombre },
                onSeleccion = { vm.onCentro(it.id) },
                error = b.errores["centro"],
                modifier = Modifier.fillMaxWidth()
            )
            CampoDropdown(
                label = "Tren de cultivo",
                textoSeleccion = trenes.firstOrNull { it.id == b.trenId }?.nombre.orEmpty(),
                opciones = trenes,
                textoOpcion = { it.nombre },
                onSeleccion = { vm.onTren(it.id) },
                enabled = b.centroId != null,
                error = b.errores["tren"],
                modifier = Modifier.fillMaxWidth()
            )
            CampoDropdown(
                label = "Línea de cultivo",
                textoSeleccion = lineas.firstOrNull { it.id == b.lineaId }?.nombre.orEmpty(),
                opciones = lineas,
                textoOpcion = { it.nombre },
                onSeleccion = { vm.onLinea(it.id) },
                enabled = b.trenId != null,
                error = b.errores["linea"],
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = b.tramo,
                onValueChange = vm::onTramo,
                label = { Text("Tramo") },
                placeholder = { Text("Ej: T1 (0-5 m)") },
                singleLine = true,
                isError = b.errores["tramo"] != null,
                supportingText = { b.errores["tramo"]?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = b.profundidad,
                    onValueChange = vm::onProfundidad,
                    label = { Text("Profundidad (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = b.errores["profundidad"] != null,
                    supportingText = { b.errores["profundidad"]?.let { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = b.talla,
                    onValueChange = vm::onTalla,
                    label = { Text("Talla prom. (mm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = b.errores["talla"] != null,
                    supportingText = { b.errores["talla"]?.let { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
            }

            // Conteo de individuos
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Conteo de individuos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(onClick = { vm.sumarCantidad(-1) }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar uno")
                        }
                        OutlinedTextField(
                            value = b.cantidad.toString(),
                            onValueChange = { texto -> vm.onCantidad(texto.filter { it.isDigit() }.toIntOrNull() ?: 0) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = b.errores["cantidad"] != null,
                            modifier = Modifier.weight(1f)
                        )
                        FilledIconButton(onClick = { vm.sumarCantidad(1) }, modifier = Modifier.size(52.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar uno")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 50).forEach { n ->
                            AssistChip(onClick = { vm.sumarCantidad(n) }, label = { Text("+$n") })
                        }
                        AssistChip(onClick = { vm.onCantidad(0) }, label = { Text("Reiniciar") })
                    }
                    b.errores["cantidad"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = b.observaciones,
                onValueChange = vm::onObservaciones,
                label = { Text("Observaciones") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { if (vm.validarFormulario()) onSiguiente() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("SIGUIENTE: EVIDENCIA")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (mostrarFecha) {
        SelectorFecha(
            fechaHoraMillis = b.fechaHora,
            onConfirmar = {
                vm.onFecha(it)
                mostrarFecha = false
            },
            onCancelar = { mostrarFecha = false }
        )
    }
    if (mostrarHora) {
        SelectorHora(
            fechaHoraMillis = b.fechaHora,
            onConfirmar = { hora, minuto ->
                vm.onHora(hora, minuto)
                mostrarHora = false
            },
            onCancelar = { mostrarHora = false }
        )
    }
}
