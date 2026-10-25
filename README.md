# Electra — Registro de pruebas eléctricas en terreno

Aplicación Android para **Induztek Ingeniería SpA** (caso CITT Plaza Norte, DSY1105 Desarrollo de
Aplicaciones Móviles). Permite que el ingeniero o técnico registre en el mismo momento cada prueba
eléctrica que hace en terreno (transformadores, relés, celdas, cables), con evidencia de foto y GPS,
consulte el historial comparable de cada equipo y formalice los protocolos. Además, puede entregar
de inmediato un respaldo al cliente sin volver a un computador.

> Todos los datos son ficticios. No se usan protocolos reales de clientes.

## Autor

Proyecto individual desarrollado por **Simón Muñoz** (diseño, desarrollo y pruebas).

## Problema que resuelve

Hoy el cliente no recibe ningún respaldo en el momento de la prueba: el técnico anota en papel y
transcribe días después. Electra:

- **Registra la prueba en terreno** con formulario validado, foto del equipo y ubicación GPS, y la deja
  en estado _registrada en terreno_.
- **Entrega un respaldo inmediato** al cliente (botón _Compartir respaldo_: correo, WhatsApp, etc.).
- **Guarda un historial trazable por equipo** y compara cada medición con la anterior del mismo tipo.
- **Formaliza protocolos** (_Registrado en terreno → Formalizado_) desde el teléfono.
- **Funciona sin conexión**: todo se guarda localmente en SQLite.

## Funcionalidades implementadas

| Requisito de la evaluación | Implementación |
|---|---|
| Interfaz organizada y navegación clara | Material 3 con paleta corporativa centralizada (`ui/theme`), barra superior común, Home con resumen real, acceso a Perfil, 13 pantallas conectadas en un `NavHost`. |
| Formularios validados con íconos y mensajes | Login, Registro, Registrar prueba, Crear protocolo y Editar observación. Cada campo muestra ícono de error rojo + mensaje animado o check verde si es válido, contador de caracteres y requisitos de contraseña en vivo. |
| Validaciones desde la lógica | Reglas puras en `validation/Validaciones.kt`; los ViewModels exponen el error de cada campo en su `UiState` y la UI solo lo pinta. |
| Animaciones | Transiciones entre pantallas (deslizamiento + fundido), sacudida del formulario al fallar, mensajes de error que se despliegan, ícono de campo con rebote, botón que se encoge al presionar y muestra progreso, barra de fortaleza de contraseña, contadores del Home, check animado en la confirmación, cambio de color de estado, listas con `animateItem`. |
| Estructura modular + persistencia local | MVVM con repositorios e inyección manual (`AppContainer`); SQLite (`PruebaDbHelper`, versión 3 con migraciones) para pruebas y protocolos; usuarios (contraseña guardada como hash SHA-256) y sesión en `SharedPreferences`. |
| Recursos nativos (≥ 2) | **Cámara** (foto de evidencia con `TakePicture` + `FileProvider`, permiso en tiempo de ejecución), **GPS** (`LocationManager`, permiso de ubicación, tiempo máximo y respaldo con la última ubicación), **vibración** (confirmación y error) y **hoja de compartir** del sistema. |

## Arquitectura

```
com.example.electra
├── data/          Repositorios (interfaces + SQLite / SharedPreferences / memoria) y AppContainer
├── device/        Recursos nativos: cámara (archivos), GPS, vibración, compartir
├── model/         Prueba, Protocolo, catálogo de equipos y textos de respaldo
├── navigation/    Rutas y NavHost con transiciones
├── validation/    Reglas de validación puras (sin Android)
└── ui/
    ├── components/  Campo validado, selector, botón con carga, barra superior, animación de sacudida
    ├── screen/      Pantallas Compose
    ├── theme/       Paleta y tema Material 3
    └── viewmodel/   ViewModels con StateFlow para cada pantalla con estado o datos persistidos
```

Más detalle en [ARCHITECTURE.md](ARCHITECTURE.md) y [PROCESO.md](PROCESO.md).

## Requisitos

- Android Studio en una versión compatible con AGP 9.4 (usa su JDK incluido).
- Android SDK 37; dispositivo o emulador con Android 7.0 (API 24) o superior.
- Para probar la cámara y el GPS en el emulador: _Extended controls → Camera / Location_.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en Android Studio (**File → Open**).
2. Esperar la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo y presionar **Run ▶** (configuración `app`).
4. En la app: **Regístrate**, crea tu cuenta, inicia sesión y usa **Registrar nueva prueba**.

Desde la terminal:

```powershell
.\gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk
.\gradlew installDebug           # instala en el dispositivo conectado
```

## Pruebas

```powershell
.\gradlew testDebugUnitTest                 # unitarias (validaciones, ViewModels, rutas)
.\gradlew connectedDebugAndroidTest         # instrumentadas (SQLite, pantallas y flujos E2E)
```

Los tests instrumentados usan almacenamiento aislado con prefijo `test_` y no tocan los datos reales.
