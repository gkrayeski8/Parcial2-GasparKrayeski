package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class ProgramaModel(
    app: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(app) {

    private val servicios = Servicios.obtener(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda

    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tieneEdicionPrevia = repo.tieneEdicionPrevia

    val dia: StateFlow<Int> = savedState.getStateFlow("filtro_dia", 0)
    val tema: StateFlow<String> = savedState.getStateFlow("filtro_tema", "Todos")

    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)

    private var cargaInicialHecha = false
    private var jobDescarga: Job? = null
    private var haySolicitudEncolada = false

    fun cambiarDia(nuevoDia: Int) {
        savedState["filtro_dia"] = nuevoDia
    }

    fun cambiarTema(nuevoTema: String) {
        savedState["filtro_tema"] = nuevoTema
    }

    fun cargarAlIniciarSesion() {
        if (cargaInicialHecha) return
        cargaInicialHecha = true
        recargar()
    }

    fun recargar() {
        if (jobDescarga?.isActive == true) {
            haySolicitudEncolada = true
            return
        }
        jobDescarga = viewModelScope.launch {
            do {
                haySolicitudEncolada = false
                cargando.value = true
                aviso.value = ""
                try {
                    repo.sincronizar()
                } catch (e: IOException) {
                    aviso.value = "No se pudo consultar el programa"
                } finally {
                    cargando.value = false
                }
            } while (haySolicitudEncolada)
        }
    }

    fun marcar(id: String) {
        viewModelScope.launch { repo.cambiarFavorita(id) }
    }
}