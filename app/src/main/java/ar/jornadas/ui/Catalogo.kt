package ar.jornadas.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.jornadas.data.Charla

@Composable
fun Catalogo(model: ProgramaModel, abrir: (String) -> Unit) {
    val dia by model.dia.collectAsStateWithLifecycle()
    val tema by model.tema.collectAsStateWithLifecycle()
    val fichas by model.charlas.collectAsStateWithLifecycle()
    val aviso by model.aviso.collectAsStateWithLifecycle()
    val cargando by model.cargando.collectAsStateWithLifecycle()
    val tieneEdicionPrevia by model.tieneEdicionPrevia.collectAsStateWithLifecycle()

    Selector(listOf("Ambos días", "Día 1", "Día 2"), dia) { model.cambiarDia(it) }
    val temas = listOf("Todos", "Android", "Datos", "Calidad")
    Selector(temas, temas.indexOf(tema).coerceAtLeast(0)) { model.cambiarTema(temas[it]) }

    TextButton(onClick = model::recargar) { Text("Actualizar") }
    if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())

    if (aviso.isNotEmpty() && !tieneEdicionPrevia) {
        Text(aviso)
        OutlinedButton(onClick = model::recargar) { Text("Reintentar") }
    } else {
        if (aviso.isNotEmpty() && tieneEdicionPrevia) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sin conexión · Usando datos guardados",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = model::recargar) { Text("Reintentar") }
            }
        }

        if (fichas.isEmpty() && !cargando) {
            Text("Sin charlas programadas")
        } else {
            val filtradas = fichas.filter {
                (dia == 0 || it.dia == dia) && (tema == "Todos" || it.tema == tema)
            }
            if (filtradas.isEmpty() && fichas.isNotEmpty()) {
                Text("No hay charlas para los filtros seleccionados.")
            } else {
                Grilla(filtradas, abrir)
            }
        }
    }
}

@Composable
private fun Selector(opciones: List<String>, indice: Int, elegir: (Int) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            FilterChip(selected = i == indice, onClick = { elegir(i) }, label = { Text(texto) })
        }
    }
}

@Composable
fun Grilla(fichas: List<Charla>, abrir: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        items(fichas, key = { it.id }) { ficha ->
            ElevatedCard(onClick = { abrir(ficha.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Día ${ficha.dia} · ${ficha.hora} · ${ficha.sala}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(ficha.titulo, style = MaterialTheme.typography.titleLarge)
                    Text(if (ficha.favorita) "${ficha.tema} · En mi agenda" else ficha.tema)
                }
            }
        }
    }
}