package cl.aquaxor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.aquaxor.app.util.BarraDia

/** Gráfico de barras (Canvas) con el valor sobre cada barra y el día debajo. */
@Composable
fun GraficoBarras(
    barras: List<BarraDia>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    colorHoy: Color = MaterialTheme.colorScheme.secondary
) {
    val maximo = (barras.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barras.forEachIndexed { i, b ->
            val esHoy = i == barras.lastIndex
            Column(
                modifier = Modifier.weight(1f).height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    if (b.total > 0) b.total.toString() else "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Canvas(Modifier.fillMaxWidth().weight(1f)) {
                    val alto = size.height * (b.total.toFloat() / maximo)
                    val altoVisible = if (b.total > 0) alto.coerceAtLeast(6.dp.toPx()) else 3.dp.toPx()
                    drawRoundRect(
                        color = if (b.total == 0) color.copy(alpha = 0.18f) else if (esHoy) colorHoy else color,
                        topLeft = Offset(size.width * 0.15f, size.height - altoVisible),
                        size = Size(size.width * 0.7f, altoVisible),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
                Text(b.etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Una porción del gráfico de dona. */
data class Porcion(val etiqueta: String, val valor: Int, val color: Color)

/** Gráfico de dona con el total al centro. */
@Composable
fun GraficoDona(
    porciones: List<Porcion>,
    modifier: Modifier = Modifier,
    tamano: Dp = 130.dp,
    centro: String = porciones.sumOf { it.valor }.toString(),
    subtitulo: String = "muestras"
) {
    val total = porciones.sumOf { it.valor }
    val fondo = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tamano)) {
            val grosor = size.minDimension * 0.17f
            val lado = size.minDimension - grosor
            val tl = Offset(grosor / 2, grosor / 2)
            if (total == 0) {
                drawArc(fondo, 0f, 360f, false, tl, Size(lado, lado), style = Stroke(grosor))
            } else {
                var inicio = -90f
                porciones.filter { it.valor > 0 }.forEach { p ->
                    val barrido = 360f * p.valor / total
                    drawArc(p.color, inicio, (barrido - 2f).coerceAtLeast(1f), false, tl, Size(lado, lado), style = Stroke(grosor))
                    inicio += barrido
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centro, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Anillo de progreso (0f..1f) con el porcentaje al centro. */
@Composable
fun AnilloProgreso(
    fraccion: Float,
    modifier: Modifier = Modifier,
    tamano: Dp = 84.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    etiqueta: String = "${(fraccion * 100).toInt()}%"
) {
    val pista = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tamano)) {
            val g = size.minDimension * 0.14f
            val lado = size.minDimension - g
            val tl = Offset(g / 2, g / 2)
            drawArc(pista, 0f, 360f, false, tl, Size(lado, lado), style = Stroke(g))
            drawArc(color, -90f, 360f * fraccion.coerceIn(0f, 1f), false, tl, Size(lado, lado), style = Stroke(g, cap = StrokeCap.Round))
        }
        Text(etiqueta, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** Barra horizontal de progreso con etiqueta y valor. */
@Composable
fun BarraHorizontal(etiqueta: String, valor: Int, fraccion: Float, color: Color, modifier: Modifier = Modifier) {
    val pista = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text(valor.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Canvas(Modifier.fillMaxWidth().height(8.dp)) {
            drawRoundRect(pista, size = size, cornerRadius = CornerRadius(4.dp.toPx()))
            drawRoundRect(color, size = Size(size.width * fraccion.coerceIn(0f, 1f), size.height), cornerRadius = CornerRadius(4.dp.toPx()))
        }
    }
}
