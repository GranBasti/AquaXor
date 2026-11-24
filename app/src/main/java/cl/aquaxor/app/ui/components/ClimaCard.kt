package cl.aquaxor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.aquaxor.app.data.network.Aptitud
import cl.aquaxor.app.data.network.Clima
import cl.aquaxor.app.data.network.Ubicacion
import cl.aquaxor.app.ui.theme.AquaVerde
import cl.aquaxor.app.viewmodel.ClimaUiState

fun iconoClima(codigo: Int): ImageVector = when (codigo) {
    0, 1 -> Icons.Default.WbSunny
    2, 3, 45, 48 -> Icons.Default.Cloud
    in 51..67, in 80..82 -> Icons.Default.WaterDrop
    in 95..99 -> Icons.Default.Thunderstorm
    else -> Icons.Default.Grain
}

fun colorAptitud(a: Aptitud): Color = when (a) {
    Aptitud.BUENA -> AquaVerde
    Aptitud.PRECAUCION -> Color(0xFFE0A100)
    Aptitud.NO_RECOMENDABLE -> Color(0xFFC0392B)
}

/** Tarjeta de clima actual (Retrofit + Open-Meteo) con temperatura de las próximas horas. */
@Composable
fun ClimaCard(estado: ClimaUiState, onActualizar: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Clima en ${Ubicacion.NOMBRE}", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                    Text("Condiciones para la faena", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (estado.cargando) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onActualizar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar clima", tint = Color.White)
                    }
                }
            }
            val clima = estado.clima
            when {
                clima != null -> ContenidoClima(clima)
                estado.error != null -> Text(estado.error, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                else -> Text("Consultando clima…", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ContenidoClima(c: Clima) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(iconoClima(c.codigo), contentDescription = c.descripcion, tint = Color.White, modifier = Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${c.temperatura.toInt()}°C", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("${c.descripcion} · sensación ${c.sensacion.toInt()}°", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DatoClima(Icons.Default.Air, "${c.vientoKmh.toInt()} km/h ${c.direccionCardinal}")
        DatoClima(Icons.Default.WaterDrop, "${c.humedad}% humedad")
        DatoClima(Icons.Default.Grain, "${"%.1f".format(c.precipitacionMm)} mm")
    }
    if (c.horaTemperatura.size >= 2) MiniLinea(c.horaTemperatura)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(12.dp).background(colorAptitud(c.aptitud), RoundedCornerShape(50)))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(c.aptitud.etiqueta, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelLarge)
            Text(c.aptitud.detalle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DatoClima(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

/** Línea simple con la temperatura horaria del día. */
@Composable
private fun MiniLinea(valores: List<Double>) {
    Column {
        Text("Temperatura de hoy (por hora)", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        Canvas(Modifier.fillMaxWidth().height(44.dp).padding(top = 4.dp)) {
            val min = valores.min()
            val rango = (valores.max() - min).coerceAtLeast(0.5)
            val paso = size.width / (valores.size - 1)
            val camino = Path()
            valores.forEachIndexed { i, v ->
                val x = i * paso
                val y = size.height - ((v - min) / rango).toFloat() * size.height
                if (i == 0) camino.moveTo(x, y) else camino.lineTo(x, y)
            }
            drawPath(camino, Color.White, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            val actual = (java.time.LocalTime.now().hour).coerceIn(0, valores.lastIndex)
            val yA = size.height - ((valores[actual] - min) / rango).toFloat() * size.height
            drawCircle(Color(0xFFF26922), 5.dp.toPx(), Offset(actual * paso, yA))
        }
    }
}
