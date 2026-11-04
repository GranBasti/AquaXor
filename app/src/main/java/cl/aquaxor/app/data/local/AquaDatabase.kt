package cl.aquaxor.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.aquaxor.app.model.Adjunto
import cl.aquaxor.app.model.Centro
import cl.aquaxor.app.model.Linea
import cl.aquaxor.app.model.Muestra
import cl.aquaxor.app.model.Tren
import cl.aquaxor.app.model.Usuario

@Database(
    entities = [Usuario::class, Centro::class, Tren::class, Linea::class, Muestra::class, Adjunto::class],
    version = 1,
    exportSchema = false
)
abstract class AquaDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun muestraDao(): MuestraDao
    abstract fun adjuntoDao(): AdjuntoDao

    companion object {
        fun crear(context: Context): AquaDatabase =
            Room.databaseBuilder(context.applicationContext, AquaDatabase::class.java, "aquaxor.db")
                .build()
    }
}
