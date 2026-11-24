package cl.aquaxor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.aquaxor.app.model.MuestraResumen
import cl.aquaxor.app.ui.theme.AquaVerde
import cl.aquaxor.app.util.Formatos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuestraCard(item: MuestraResumen, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val m = item.muestra
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
        Miniatura(item.primeraFoto, Modifier.size(72.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Muestra #${m.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(m.estado)
            }
            FilaIcono(Icons.Default.LocationOn, "${item.centroNombre} · ${item.trenNombre} · ${item.lineaNombre}")
            FilaIcono(Icons.Default.Schedule, Formatos.fechaHora(m.fechaHora), secundario = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaIcono(Icons.Default.LinearScale, "Tramo ${m.tramo}", secundario = true)
                FilaIcono(Icons.Default.Person, item.operadorNombre, secundario = true)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "${m.cantidadIndividuos} individuos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                IconoConTexto(Icons.Default.PhotoCamera, item.totalFotos.toString())
                IconoConTexto(Icons.Default.Description, item.totalDocumentos.toString())
                Icon(
                    imageVector = if (m.sincronizada) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = if (m.sincronizada) "Sincronizada" else "Pendiente de sincronizar",
                    tint = if (m.sincronizada) AquaVerde else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        }
    }
}

/** Foto de la muestra (primera evidencia) o un ícono si no tiene. */
@Composable
private fun Miniatura(ruta: String?, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(12.dp)
    if (ruta != null) {
        AsyncImage(
            model = File(ruta),
            contentDescription = "Foto de la muestra",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(forma)
        )
    } else {
        Box(
            modifier.clip(forma).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun FilaIcono(icono: ImageVector, texto: String, secundario: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(5.dp))
        Text(
            texto,
            style = if (secundario) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = if (secundario) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun IconoConTexto(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
