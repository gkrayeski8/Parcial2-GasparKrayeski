# Registro de cambios · Jornadas del Sur

## Arquitectura y Buenas Prácticas Aplicadas
- **Flujo Unidireccional de Datos (UDF) y `StateFlow`:** El `ViewModel` (`ProgramaModel`) administra el estado mediante `MutableStateFlow` interno con propiedad de respaldo (*Backing Property*) y expone un `StateFlow` inmutable hacia la capa de presentación en Jetpack Compose (`UI = f(State)`), recolectado respetando el ciclo de vida (`collectAsStateWithLifecycle()`).
- **Resiliencia *Offline-First* y Fuente Única de Verdad:** Se separó el estado remoto obtenido vía Retrofit del estado local persistente, asegurando disponibilidad ante cortes de red y reconciliación consistente por identificador estable.
- **Gestión del Ciclo de Vida (*Configuration Changes* y *Process Death*):** Se evitó el acoplamiento de peticiones de red al ciclo de vida de la vista y se incorporó `SavedStateHandle` para sobrevivir tanto a rotaciones de pantalla como a la destrucción del proceso en segundo plano por el SO.
- **Consumo Seguro de Red y Concurrencia:** Las peticiones `suspend` de Retrofit se ejecutan fuera del hilo principal con captura defensiva de excepciones (`IOException` / `HttpException`) y control de concurrencia para evitar solicitudes duplicadas.

---

## Detalle por Condición de Entrega

### 41 · Programa sin señal
- **Persistencia local de catálogo y edición válida:** Se almacenan localmente las charlas obtenidas y una bandera de edición válida descargada.
- **Manejo defensivo de fallos de red:**
  - Ante una falla de conectividad con edición previa guardada, se conservan la lista, el detalle y las charlas agendadas, informando al usuario que se muestran datos guardados con opción de reintento.
  - Sin descarga previa, se presenta el estado de error con acción de reintento.
  - Una edición vacía exitosa (`v4`) se persiste como edición válida mostrando *«Sin charlas programadas»* y mantiene ese estado al reabrir sin señal.

### 42 · Agenda personal
- **Identificadores estables y reconciliación:** Las marcas de *Mi agenda* se persisten asociadas al `id` estable de cada charla.
- **Actualización entre revisiones:**
  - Al pasar de `v1` a `v2`, la charla `11` conserva su marca en *Mi agenda* actualizando su título y sala.
  - Al pasar a `v3`, las charlas retiradas del catálogo (como la `11`) se depuran de *Mi agenda*, preservando las marcas vigentes e incorporando las nuevas (`90`) sin marcar. Si una charla retirada reingresa posteriormente, lo hace sin marcar.
- **Consistencia ante concurrencia:** Las marcas agregadas o quitadas mientras hay una petición HTTP en vuelo se aplican sobre el estado vigente y se respetan al completarse la respuesta.

### 43 · Volver al punto de lectura
- **Restauración con `SavedStateHandle`:** El filtro de día, el filtro de tema y el identificador de la charla abierta en detalle se resguardan en `SavedStateHandle`.
- **Comportamiento ante rotación y *Process Death*:**
  - Al regresar del detalle y rotar el dispositivo, se mantienen ambos filtros activos.
  - Si Android destruye el proceso en segundo plano y el usuario vuelve desde *Recientes*, se restaura la ficha abierta y, al volver, se conservan ambos filtros. Una tarea nueva inicia sin filtros.

### 44 · Control de descargas
- **Control de ciclo de vida y deduplicación:**
  - Se ejecuta una única carga automática al iniciar la sesión/proceso; rotar, abrir una ficha o cambiar de pestaña genera cero peticiones adicionales.
  - Las actualizaciones explícitas disparadas durante una descarga en curso se coalescen en un único intento pendiente tras finalizar la petición activa.
  - Al finalizar con éxito o falla se rehabilitan las actualizaciones explícitas, manteniendo los filtros editables durante la espera.

### Ajustes de compilación
- **`ServicioLocal.kt`:** Se adaptó la construcción de `MediaType` y `ResponseBody` a la firma de OkHttp 3 (`MediaType.parse` y `ResponseBody.create`) incluida por las dependencias del proyecto para permitir la compilación con `./gradlew assembleDebug`.
