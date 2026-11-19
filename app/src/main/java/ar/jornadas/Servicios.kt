package ar.jornadas

import android.content.Context
import androidx.room.Room
import ar.jornadas.data.*
import ar.jornadas.lab.ServicioLocal
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Servicios(context: Context) {
    private val appContext = context.applicationContext
    val mesa = ServicioLocal(appContext)
    private val archivo = Room.databaseBuilder(appContext, Archivo::class.java, "cartelera.db").build()
    private val api = Retrofit.Builder().baseUrl("https://programa.invalid/")
        .client(OkHttpClient.Builder().addInterceptor(mesa).build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(ProgramaApi::class.java)
    val agenda = AgendaRepository(appContext, api, archivo.cartelera())

    companion object {
        @Volatile private var instancia: Servicios? = null
        fun obtener(context: Context): Servicios =
            instancia ?: synchronized(this) {
                instancia ?: Servicios(context).also { instancia = it }
            }
    }
}