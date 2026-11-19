package ar.jornadas.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgendaRepository(
    context: Context,
    private val api: ProgramaApi,
    private val dao: CarteleraDao
) {
    private val prefs = context.getSharedPreferences("agenda_meta", Context.MODE_PRIVATE)
    private val _tieneEdicionPrevia = MutableStateFlow(prefs.getBoolean("edicion_valida", false))
    val tieneEdicionPrevia = _tieneEdicionPrevia.asStateFlow()

    val programa = dao.observar()

    suspend fun sincronizar() {
        val fichas = api.consultar().map {
            Charla(it.id, it.title, it.track, it.day, it.time, it.room, it.speaker, it.summary)
        }
        dao.sincronizarInstantanea(fichas)
        prefs.edit().putBoolean("edicion_valida", true).apply()
        _tieneEdicionPrevia.value = true
    }

    suspend fun cambiarFavorita(id: String) = dao.alternar(id)
}
