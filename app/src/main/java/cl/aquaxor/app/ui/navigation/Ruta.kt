package cl.aquaxor.app.ui.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Main : Ruta("main")
    data object Form : Ruta("form")
    data object Evidencia : Ruta("evidencia")
    data object Historial : Ruta("historial")
    data object Estados : Ruta("estados")
    data object Admin : Ruta("admin")
    data object Detalle : Ruta("detalle/{id}") {
        fun crear(id: Long): String = "detalle/$id"
    }
}
