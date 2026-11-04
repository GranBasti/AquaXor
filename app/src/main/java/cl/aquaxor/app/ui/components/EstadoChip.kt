package cl.aquaxor.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.aquaxor.app.model.EstadoRevision

fun colorEstado(estado: EstadoRevision): Pair<Color, Color> = when (estado) {
    EstadoRevision.PENDIENTE -> Color(0xFFFFE4D4) to Color(0xFF8A3A00)
    EstadoRevision.EN_REVISION -> Color(0xFFD6E9FF) to Color(0xFF0D4F8B)
    EstadoRevision.APROBADA -> Color(0xFFCFF0DB) to Color(0xFF16633A)
    EstadoRevision.OBSERVADA -> Color(0xFFFADBD8) to Color(0xFF8E1F14)
}

@Composable
fun EstadoChip(estado: EstadoRevision, modifier: Modifier = Modifier) {
    val (fondo, texto) = colorEstado(estado)
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = estado.etiqueta,
            color = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
