package ar.jornadas.data

import androidx.room.*
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow

data class FichaRemota(
    val id: String,
    val title: String,
    val track: String,
    val day: Int,
    val time: String,
    val room: String,
    val speaker: String,
    val summary: String
)

interface ProgramaApi {
    @GET("programa")
    suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(
    @PrimaryKey val id: String,
    val titulo: String,
    val tema: String,
    val dia: Int,
    val hora: String,
    val sala: String,
    val ponente: String,
    val resumen: String,
    val favorita: Boolean = false
)

@Dao
interface CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    fun observar(): Flow<List<Charla>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarNuevas(fichas: List<Charla>)

    @Query("""
        UPDATE charlas 
        SET titulo = :titulo, tema = :tema, dia = :dia, hora = :hora, 
            sala = :sala, ponente = :ponente, resumen = :resumen 
        WHERE id = :id
    """)
    suspend fun actualizarDatosRemotos(
        id: String,
        titulo: String,
        tema: String,
        dia: Int,
        hora: String,
        sala: String,
        ponente: String,
        resumen: String
    )

    @Query("DELETE FROM charlas WHERE id NOT IN (:idsVigentes)")
    suspend fun eliminarObsoletas(idsVigentes: List<String>)

    @Query("DELETE FROM charlas")
    suspend fun eliminarTodas()

    @Transaction
    suspend fun sincronizarInstantanea(fichas: List<Charla>) {
        if (fichas.isEmpty()) {
            eliminarTodas()
            return
        }
        eliminarObsoletas(fichas.map { it.id })
        insertarNuevas(fichas)
        fichas.forEach { f ->
            actualizarDatosRemotos(
                id = f.id,
                titulo = f.titulo,
                tema = f.tema,
                dia = f.dia,
                hora = f.hora,
                sala = f.sala,
                ponente = f.ponente,
                resumen = f.resumen
            )
        }
    }

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternar(id: String)
}

@Database(entities = [Charla::class], version = 1, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}
