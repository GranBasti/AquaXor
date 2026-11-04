package cl.aquaxor.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["rut"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rut: String,
    val nombre: String,
    val passwordHash: String,
    val rol: Rol,
    val activo: Boolean = true
)

@Entity(tableName = "centros")
data class Centro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "trenes",
    foreignKeys = [
        ForeignKey(
            entity = Centro::class,
            parentColumns = ["id"],
            childColumns = ["centroId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("centroId")]
)
data class Tren(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroId: Long,
    val nombre: String
)

@Entity(
    tableName = "lineas",
    foreignKeys = [
        ForeignKey(
            entity = Tren::class,
            parentColumns = ["id"],
            childColumns = ["trenId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("trenId")]
)
data class Linea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trenId: Long,
    val nombre: String
)

@Entity(
    tableName = "muestras",
    foreignKeys = [
        ForeignKey(
            entity = Centro::class,
            parentColumns = ["id"],
            childColumns = ["centroId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Tren::class,
            parentColumns = ["id"],
            childColumns = ["trenId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Linea::class,
            parentColumns = ["id"],
            childColumns = ["lineaId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("centroId"), Index("trenId"), Index("lineaId"), Index("usuarioId"), Index("fechaHora")]
)
data class Muestra(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroId: Long,
    val trenId: Long,
    val lineaId: Long,
    val usuarioId: Long,
    /** Fecha y hora del muestreo en milisegundos (epoch). */
    val fechaHora: Long,
    val tramo: String,
    val profundidadM: Double? = null,
    val tallaMm: Double? = null,
    /** Conteo de choritos (individuos) de la muestra. */
    val cantidadIndividuos: Int,
    val observaciones: String = "",
    val estado: EstadoRevision = EstadoRevision.PENDIENTE,
    val sincronizada: Boolean = false
)

@Entity(
    tableName = "adjuntos",
    foreignKeys = [
        ForeignKey(
            entity = Muestra::class,
            parentColumns = ["id"],
            childColumns = ["muestraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("muestraId")]
)
data class Adjunto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val muestraId: Long,
    val tipo: TipoAdjunto,
    /** Ruta absoluta del archivo copiado al almacenamiento privado de la app. */
    val ruta: String,
    val nombre: String,
    val creadoEn: Long
)
