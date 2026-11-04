package cl.aquaxor.app.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import cl.aquaxor.app.util.Formatos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ArchivoGuardado(val ruta: String, val nombre: String)

/**
 * Maneja los archivos de la app (fotos de cámara, imágenes de galería y documentos).
 * Todo se copia al almacenamiento privado para no depender de permisos de URIs externas.
 */
class FileStorage(private val context: Context) {

    private val authority = "${context.packageName}.fileprovider"

    private val carpeta: File
        get() = File(context.filesDir, "adjuntos").apply { mkdirs() }

    fun nuevoArchivoFoto(): File = File(carpeta, "FOTO_${Formatos.marcaArchivo()}.jpg")

    fun uri(ruta: String): Uri = FileProvider.getUriForFile(context, authority, File(ruta))

    fun mime(ruta: String): String {
        val extension = ruta.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    fun eliminar(ruta: String) {
        runCatching { File(ruta).delete() }
    }

    /** Copia el contenido de una URI (galería o selector de documentos) a la carpeta privada. */
    suspend fun copiarDesdeUri(uri: Uri, prefijo: String): ArchivoGuardado? = withContext(Dispatchers.IO) {
        val nombreOriginal = nombreVisible(uri) ?: "archivo"
        var extension = nombreOriginal.substringAfterLast('.', "")
        if (extension.isEmpty()) {
            val tipo = context.contentResolver.getType(uri)
            extension = tipo?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: ""
        }
        val sufijo = if (extension.isNotEmpty()) ".$extension" else ""
        val destino = File(carpeta, "${prefijo}_${Formatos.marcaArchivo()}_${System.nanoTime() % 10_000}$sufijo")
        try {
            val entrada = context.contentResolver.openInputStream(uri) ?: return@withContext null
            entrada.use { input -> destino.outputStream().use { salida -> input.copyTo(salida) } }
            ArchivoGuardado(destino.absolutePath, nombreOriginal)
        } catch (e: Exception) {
            destino.delete()
            null
        }
    }

    /** Escribe texto en una URI elegida por el usuario (CreateDocument). */
    suspend fun escribir(uri: Uri, contenido: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val salida = context.contentResolver.openOutputStream(uri) ?: return@withContext false
            salida.use { it.write(contenido.toByteArray(Charsets.UTF_8)) }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun nombreVisible(uri: Uri): String? =
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        } catch (e: Exception) {
            null
        }
}
