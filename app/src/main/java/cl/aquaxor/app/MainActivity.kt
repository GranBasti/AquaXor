package cl.aquaxor.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.aquaxor.app.ui.navigation.AppNavGraph
import cl.aquaxor.app.ui.theme.AquaXorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barra de estado transparente con íconos claros (la TopAppBar es azul)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val container = (application as AquaXorApp).container
        setContent {
            AquaXorTheme {
                AppNavGraph(container)
            }
        }
    }
}
