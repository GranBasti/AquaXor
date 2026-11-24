package cl.aquaxor.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lista desplegable compacta (alto 52 dp). Su ancho lo da quien la usa (weight) y el menú
 * se ajusta al texto de las opciones, en vez de ocupar toda la pantalla.
 */
@Composable
fun <T> DropdownCompacto(
    etiqueta: String,
    icono: ImageVector,
    textoSeleccion: String,
    opciones: List<T>,
    textoOpcion: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    llenar: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }
    val colorBorde = when {
        error != null -> MaterialTheme.colorScheme.error
        enabled -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(modifier) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(if (llenar) Modifier.weight(1f).fillMaxWidth() else Modifier) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, colorBorde),
                modifier = (if (llenar) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(52.dp))
                    .clickable(enabled = enabled) { expandido = true }
            ) {
                Row(Modifier.padding(start = 8.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icono, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        textoSeleccion.ifBlank { "Elegir" },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (textoSeleccion.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expandido && enabled, onDismissRequest = { expandido = false }) {
                if (opciones.isEmpty()) {
                    DropdownMenuItem(text = { Text("Sin opciones") }, onClick = { expandido = false }, enabled = false)
                }
                opciones.forEach { o ->
                    DropdownMenuItem(
                        text = { Text(textoOpcion(o)) },
                        onClick = { onSeleccion(o); expandido = false }
                    )
                }
            }
        }
    }
}

/** Campo de texto compacto (alto 52 dp) con etiqueta arriba e ícono. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoCompacto(
    etiqueta: String,
    icono: ImageVector,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    teclado: KeyboardOptions = KeyboardOptions.Default,
    error: String? = null,
    centrado: Boolean = false,
    alto: androidx.compose.ui.unit.Dp = 52.dp,
    unaLinea: Boolean = true,
    llenar: Boolean = false,
    tamanoTexto: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    val fuente = MaterialTheme.typography.bodyMedium.copy(
        fontSize = tamanoTexto,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = if (centrado) TextAlign.Center else TextAlign.Start
    )
    val interaccion = remember { MutableInteractionSource() }
    Column(modifier) {
        if (etiqueta.isNotEmpty()) Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        BasicTextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = unaLinea,
            textStyle = fuente,
            keyboardOptions = teclado,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interaccion,
            modifier = Modifier.fillMaxWidth().then(if (llenar) Modifier.weight(1f) else Modifier.height(alto)),
            decorationBox = { interior ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = valor,
                    innerTextField = interior,
                    enabled = true,
                    singleLine = unaLinea,
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                    interactionSource = interaccion,
                    isError = error != null,
                    placeholder = { Text(placeholder, style = fuente.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)) },
                    leadingIcon = { Icon(icono, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = error != null,
                            interactionSource = interaccion,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                )
            }
        )
    }
}
