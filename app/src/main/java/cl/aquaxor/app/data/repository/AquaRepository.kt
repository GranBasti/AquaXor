package cl.aquaxor.app.data.repository

import androidx.room.withTransaction
import cl.aquaxor.app.data.local.AquaDatabase
import cl.aquaxor.app.model.Adjunto
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.ConteoEstado
import cl.aquaxor.app.model.EstadoRevision
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.MuestraResumen
import cl.aquaxor.app.model.Rol
import cl.aquaxor.app.model.Tren
import cl.aquaxor.app.model.Usuario
import cl.aquaxor.app.util.Seguridad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Única fuente de datos de la app (Room / SQLite).
 * Los ViewModels nunca tocan los DAO directamente.
 */
class AquaRepository(private val db: AquaDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val catalogoDao = db.catalogoDao()
    private val muestraDao = db.muestraDao()
    private val adjuntoDao = db.adjuntoDao()
    private val mutexSemilla = Mutex()

    // ---------------- Autenticación ----------------
    suspend fun login(rut: String, password: String): Usuario? {
        sembrarSiVacio()
        val usuario = usuarioDao.porRut(rut) ?: return null
        return if (usuario.passwordHash == Seguridad.hash(password)) usuario else null
    }

    // ---------------- Catálogos ----------------
    fun centros(): Flow<List<Centro>> = catalogoDao.centros()
    fun trenes(): Flow<List<Tren>> = catalogoDao.trenes()
    fun trenesDe(centroId: Long): Flow<List<Tren>> = catalogoDao.trenesDe(centroId)
    fun lineas(): Flow<List<Linea>> = catalogoDao.lineas()
    fun lineasDe(trenId: Long): Flow<List<Linea>> = catalogoDao.lineasDe(trenId)
    fun usuarios(): Flow<List<Usuario>> = usuarioDao.observarTodos()

    suspend fun guardarCentro(centro: Centro) {
        if (centro.id == 0L) catalogoDao.insertarCentro(centro) else catalogoDao.actualizarCentro(centro)
    }

    suspend fun eliminarCentro(centro: Centro) = catalogoDao.eliminarCentro(centro)

    suspend fun guardarTren(tren: Tren) {
        if (tren.id == 0L) catalogoDao.insertarTren(tren) else catalogoDao.actualizarTren(tren)
    }

    suspend fun eliminarTren(tren: Tren) = catalogoDao.eliminarTren(tren)

    suspend fun guardarLinea(linea: Linea) {
        if (linea.id == 0L) catalogoDao.insertarLinea(linea) else catalogoDao.actualizarLinea(linea)
    }

    suspend fun eliminarLinea(linea: Linea) = catalogoDao.eliminarLinea(linea)

    suspend fun guardarUsuario(usuario: Usuario) {
        if (usuario.id == 0L) usuarioDao.insertar(usuario) else usuarioDao.actualizar(usuario)
    }

    suspend fun eliminarUsuario(usuario: Usuario) = usuarioDao.eliminar(usuario)

    // ---------------- Muestras ----------------
    fun buscar(texto: String, estado: EstadoRevision?, desde: Long, centroId: Long?): Flow<List<MuestraResumen>> =
        muestraDao.buscar(texto, estado?.name, desde, centroId)

    fun observarResumen(id: Long): Flow<MuestraResumen?> = muestraDao.observarResumen(id)
    suspend fun obtener(id: Long): Muestra? = muestraDao.obtener(id)

    fun adjuntos(muestraId: Long): Flow<List<Adjunto>> = adjuntoDao.observarDe(muestraId)
    suspend fun adjuntosDe(muestraId: Long): List<Adjunto> = adjuntoDao.listaDe(muestraId)

    /** Inserta o actualiza la muestra y sus adjuntos en una sola transacción. */
    suspend fun guardarMuestra(muestra: Muestra, nuevos: List<Adjunto>, idsEliminados: List<Long>): Long =
        db.withTransaction {
            val id = if (muestra.id == 0L) {
                muestraDao.insertar(muestra)
            } else {
                muestraDao.actualizar(muestra)
                muestra.id
            }
            if (idsEliminados.isNotEmpty()) adjuntoDao.eliminar(idsEliminados)
            if (nuevos.isNotEmpty()) adjuntoDao.insertar(nuevos.map { it.copy(muestraId = id) })
            id
        }

    /** Elimina la muestra y devuelve sus adjuntos para que el llamador borre los archivos. */
    suspend fun eliminarMuestra(id: Long): List<Adjunto> =
        db.withTransaction {
            val adjuntos = adjuntoDao.listaDe(id)
            muestraDao.eliminar(id)
            adjuntos
        }

    suspend fun cambiarEstado(id: Long, estado: EstadoRevision) = muestraDao.cambiarEstado(id, estado.name)
    suspend fun marcarSincronizadas(): Int = muestraDao.marcarSincronizadas()

    // ---------------- Indicadores ----------------
    fun totalMuestras(): Flow<Int> = muestraDao.totalMuestras()
    fun conteoPorEstado(): Flow<List<ConteoEstado>> = muestraDao.conteoPorEstado()
    fun individuosDesde(desde: Long): Flow<Int> = muestraDao.individuosDesde(desde)
    fun sinSincronizar(): Flow<Int> = muestraDao.sinSincronizar()

    // ---------------- Datos de demostración (ficticios) ----------------
    private class Demo(
        val indiceLinea: Int,
        val diasAtras: Int,
        val tramo: String,
        val profundidad: Double,
        val talla: Double,
        val cantidad: Int,
        val estado: EstadoRevision,
        val obs: String
    )

    suspend fun sembrarSiVacio() = mutexSemilla.withLock {
        if (usuarioDao.contar() > 0) return@withLock
        db.withTransaction {
            usuarioDao.insertar(Usuario(rut = "111111111", nombre = "Admin AquaXor", passwordHash = Seguridad.hash("admin123"), rol = Rol.ADMINISTRADOR))
            val operadorId = usuarioDao.insertar(Usuario(rut = "222222222", nombre = "Operario Demo", passwordHash = Seguridad.hash("oper123"), rol = Rol.OPERADOR))
            usuarioDao.insertar(Usuario(rut = "333333333", nombre = "Analista Demo", passwordHash = Seguridad.hash("analista123"), rol = Rol.ANALISTA))

            // Cada elemento: (centroId, trenId, lineaId)
            val lineas = mutableListOf<Triple<Long, Long, Long>>()
            listOf("Centro Bahía Azul", "Centro Canal Norte").forEach { nombreCentro ->
                val centroId = catalogoDao.insertarCentro(Centro(nombre = nombreCentro))
                listOf("Tren A", "Tren B").forEach { nombreTren ->
                    val trenId = catalogoDao.insertarTren(Tren(centroId = centroId, nombre = nombreTren))
                    (1..3).forEach { n ->
                        val lineaId = catalogoDao.insertarLinea(Linea(trenId = trenId, nombre = "Línea $n"))
                        lineas += Triple(centroId, trenId, lineaId)
                    }
                }
            }

            val ahora = System.currentTimeMillis()
            val dia = 86_400_000L
            val demo = listOf(
                Demo(0, 0, "T1 (0-5 m)", 4.0, 44.0, 312, EstadoRevision.PENDIENTE, "Cuerda con buen asentamiento"),
                Demo(1, 1, "T2 (5-10 m)", 8.0, 41.5, 287, EstadoRevision.EN_REVISION, ""),
                Demo(3, 2, "T1 (0-5 m)", 5.0, 38.0, 254, EstadoRevision.APROBADA, "Conteo verificado con foto"),
                Demo(4, 4, "T3 (10-15 m)", 12.0, 36.2, 198, EstadoRevision.OBSERVADA, "Foto borrosa, repetir"),
                Demo(6, 9, "T2 (5-10 m)", 6.0, 47.0, 341, EstadoRevision.APROBADA, ""),
                Demo(7, 20, "T1 (0-5 m)", 4.5, 40.0, 276, EstadoRevision.EN_REVISION, "")
            )
            demo.forEach { d ->
                val (centroId, trenId, lineaId) = lineas[d.indiceLinea]
                muestraDao.insertar(
                    Muestra(
                        centroId = centroId,
                        trenId = trenId,
                        lineaId = lineaId,
                        usuarioId = operadorId,
                        fechaHora = ahora - d.diasAtras * dia - 2 * 3_600_000L,
                        tramo = d.tramo,
                        profundidadM = d.profundidad,
                        tallaMm = d.talla,
                        cantidadIndividuos = d.cantidad,
                        observaciones = d.obs,
                        estado = d.estado,
                        sincronizada = d.estado == EstadoRevision.APROBADA
                    )
                )
            }
        }
    }
}
