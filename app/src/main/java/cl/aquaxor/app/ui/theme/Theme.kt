package cl.aquaxor.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta definida por el equipo (Clase 2)
val AquaAzul = Color(0xFF004B87)      // Principal: barras, botones primarios
val AquaNaranja = Color(0xFFF26922)   // Secundario: FAB, alertas
val AquaFondo = Color(0xFFF4F8FB)     // Fondo de pantallas
val AquaTexto = Color(0xFF1C2833)     // Texto
val AquaVerde = Color(0xFF27AE60)     // Éxito / estados positivos

private val Esquema = lightColorScheme(
    primary = AquaAzul,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4F5),
    onPrimaryContainer = Color(0xFF00243F),
    secondary = AquaNaranja,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDCB),
    onSecondaryContainer = Color(0xFF4A1A00),
    tertiary = AquaVerde,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF0DB),
    onTertiaryContainer = Color(0xFF0B3D1F),
    background = AquaFondo,
    onBackground = AquaTexto,
    surface = Color.White,
    onSurface = AquaTexto,
    surfaceVariant = Color(0xFFE3ECF4),
    onSurfaceVariant = Color(0xFF3F4B57),
    outline = Color(0xFF7A8794)
)

@Composable
fun AquaXorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}
