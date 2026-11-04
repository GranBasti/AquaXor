package cl.aquaxor.app.model

enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador técnico"),
    ANALISTA("Analista de reportes")
}

enum class EstadoRevision(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    APROBADA("Aprobada"),
    OBSERVADA("Observada")
}

enum class TipoAdjunto { FOTO, DOCUMENTO }

/** Permisos por rol (Clase 1: Administrador, Operador técnico, Analista de reportes). */
fun Rol.puedeRegistrar(): Boolean = this == Rol.OPERADOR || this == Rol.ADMINISTRADOR
fun Rol.puedeEliminar(): Boolean = this == Rol.ADMINISTRADOR
fun Rol.esAdministrador(): Boolean = this == Rol.ADMINISTRADOR

/** El operador solo puede enviar a revisión; analista y administrador pueden usar todos los estados. */
fun Rol.estadosPermitidos(): List<EstadoRevision> = when (this) {
    Rol.OPERADOR -> listOf(EstadoRevision.PENDIENTE, EstadoRevision.EN_REVISION)
    else -> EstadoRevision.entries
}
