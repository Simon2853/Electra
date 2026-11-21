# Electra — Registro de pruebas eléctricas en terreno

Aplicación Android para **Induztek Ingeniería SpA** (caso CITT Plaza Norte, DSY1105 Desarrollo de
Aplicaciones Móviles). Permite que el ingeniero o técnico registre en el mismo momento cada prueba
eléctrica que hace en terreno (transformadores, relés, celdas, cables), con evidencia de foto y GPS,
consulte el historial comparable de cada equipo y formalice los protocolos. Además, puede entregar
de inmediato un respaldo al cliente sin volver a un computador.

Los datos viven en un **backend de microservicios** (Spring Boot + MySQL) al que la app se conecta
con **Retrofit** a través de un **API Gateway**:
👉 [Electra-Backend](https://github.com/Simon2853/Electra-Backend) (repositorio aparte, con su propia guía para levantarlo).

> Todos los datos son ficticios. No se usan protocolos reales de clientes.

## Autor

Proyecto desarrollado por **Simón Muñoz** (diseño, desarrollo y pruebas).

## Problema que resuelve

Hoy el cliente no recibe ningún respaldo en el momento de la prueba: el técnico anota en papel y
transcribe días después. Electra:

- **Registra la prueba en terreno** con formulario validado, foto del equipo, ubicación GPS y el clima
  del lugar, y la deja en estado _registrada en terreno_.
- **Entrega un respaldo inmediato** al cliente (botón _Compartir respaldo_: correo, WhatsApp, etc.).
- **Guarda un historial trazable por equipo** en el servidor y compara cada medición con la anterior del mismo tipo.
- **Formaliza protocolos** (_Registrado en terreno → Formalizado_) desde el teléfono; solo el supervisor
  o el administrador pueden hacerlo y, una vez formalizado, ya no se modifica.
- **Comparte los datos entre usuarios y dispositivos**: lo que registra el técnico lo revisa el supervisor desde su propio teléfono.

## Funcionalidades implementadas

| Requisito de la evaluación | Implementación |
|---|---|
| Consumo de microservicios con Retrofit | `data/remote/`: una interfaz Retrofit por microservicio (`Apis.kt`), DTOs (`Dtos.kt`) y repositorios `Remoto*` que reemplazan a los locales sin cambiar ViewModels ni pantallas. Todas las llamadas van al **API Gateway** (puerto 8080); la URL se configura en un solo lugar (`ConfiguracionApi.BASE_URL`). |
| Manejo de errores de red | `Conexion.llamar` captura los fallos de red y los 502/503/504 del Gateway: la app no se cae y muestra la franja _"Sin conexión con el servidor"_. Mientras dure, reintenta cada 10 s y, al volver el servidor, la pantalla abierta recarga sus datos sola. |
| Fotos en el servidor | La foto de evidencia y la de perfil se suben a `archivos-service` (multipart) y se muestran con **Coil** usando el mismo cliente HTTP que Retrofit. |
| API externa | **Open-Meteo** (`data/remote/OpenMeteo.kt`, sin clave): al capturar el GPS en _Registrar prueba_ se muestran temperatura, humedad y estado del tiempo; con humedad ≥ 80 % o lluvia se advierte que el ambiente puede alterar la medición, y el técnico puede agregarlo a las observaciones. Va directo a Internet, no pasa por el Gateway. |
| Splash Screen | `core-splashscreen` con el logo de Electra (`Theme.Electra.Splash`), en modo claro y oscuro. |
| Pruebas unitarias | **78 pruebas** JUnit en 4 archivos de `app/src/test` (por funcionalidad), con servidor falso; **91 % de cobertura** de líneas del código lógico medida con JaCoCo (mínimo exigido 80 %). |
| Interfaz organizada y navegación clara | Material 3 con paleta corporativa centralizada (`ui/theme`), barra superior común, Home con resumen real, 16 pantallas conectadas en un `NavHost`, tema claro / oscuro / del sistema. |
| Formularios validados con íconos y mensajes | Login, Registro, Recuperar contraseña, Editar perfil, Cambiar contraseña, Registrar prueba, Crear protocolo y Editar observación. Cada campo muestra ícono de error rojo + mensaje animado o check verde si es válido. Las reglas se validan en la app (`validation/Validaciones.kt`) **y de nuevo en el servidor**. |
| Roles y permisos | Técnico, Supervisor y Administrador (`model/Rol.kt`). El servidor aplica la misma regla: el técnico solo ve lo suyo, el supervisor ve todo y formaliza, el administrador además gestiona usuarios. |
| Animaciones | Transiciones entre pantallas, sacudida del formulario al fallar, mensajes de error que se despliegan, botón con progreso, barra de fortaleza de contraseña, contadores del Home, franja de conexión que aparece y desaparece. |
| Recursos nativos (≥ 2) | **Cámara** (`TakePicture` + `FileProvider`), **GPS** (`LocationManager`, con tiempo máximo y respaldo con la última ubicación), **vibración** y **hoja de compartir** del sistema. |

## Arquitectura

```mermaid
flowchart LR
    subgraph App Android
        UI[Pantallas Compose] --> VM[ViewModels]
        VM --> R[Repositorios<br/>interfaces]
        R --> RR[Remoto* + Retrofit]
        R --> OM[RemotoClimaRepository]
        R --> SP[(SharedPreferences<br/>sesión y tema)]
    end
    RR -- "HTTP / JSON" --> GW[API Gateway :8080]
    GW --> MS[usuarios · pruebas · protocolos · archivos<br/>microservicios + MySQL]
    OM -- "HTTPS" --> EXT[Open-Meteo<br/>API externa]
```

```
com.example.electra
├── data/          Interfaces de repositorio, AppContainer, sesión/tema y repositorios en memoria (pruebas)
│   └── remote/    Retrofit: configuración, APIs, DTOs, Conexion, repositorios remotos y Open-Meteo
├── device/        Recursos nativos: cámara (archivos), GPS, vibración, compartir
├── model/         Prueba, Protocolo, Rol, Clima, catálogo de equipos y textos de respaldo
├── navigation/    Rutas y NavHost con transiciones
├── validation/    Reglas de validación puras (sin Android)
└── ui/
    ├── components/  Campo validado, selector, botón con carga, barra superior, aviso sin conexión
    ├── screen/      Pantallas Compose
    ├── theme/       Paleta y tema Material 3
    └── viewmodel/   ViewModels con StateFlow para cada pantalla con estado o datos
```

Más detalle en [ARCHITECTURE.md](ARCHITECTURE.md) y [PROCESO.md](PROCESO.md).

## Requisitos

- Android Studio en una versión compatible con AGP 9.4 (usa su JDK incluido).
- Android SDK 37; dispositivo o emulador con Android 7.0 (API 24) o superior.
- El **backend levantado** (ver el README de [Electra-Backend](https://github.com/Simon2853/Electra-Backend):
  `iniciar-todo.cmd` o paso a paso) y MySQL encendido en Laragon.
- Para probar la cámara y el GPS en el emulador: _Extended controls → Camera / Location_.
- Para el clima: el emulador o teléfono necesita acceso a Internet.

## Cómo ejecutar

1. Levantar el backend y comprobar que en http://localhost:8761 aparezcan los 5 servicios en estado UP.
2. Revisar la dirección del Gateway en `app/src/main/java/com/example/electra/data/remote/ConfiguracionApi.kt`:
   - **Emulador** (backend en el mismo computador): `http://10.0.2.2:8080/` (valor por defecto).
   - **Teléfono físico**: la URL pública del reenvío del puerto 8080 en VS Code (pestaña _Puertos_,
     visibilidad _Public_), por ejemplo `https://abc123-8080.brs.devtunnels.ms/`. Debe terminar en `/`
     y cambia cada vez que se crea el túnel.
3. Abrir el proyecto en Android Studio, esperar la sincronización de Gradle, elegir el dispositivo y presionar **Run ▶**.
4. En Android 17 la app pide el permiso de **dispositivos cercanos / red local**: hay que aceptarlo
   para llegar a `10.0.2.2` (con la URL pública no se pide).
5. En la app: **Regístrate**, crea tu cuenta, inicia sesión y usa **Registrar nueva prueba**.

La sesión queda guardada en el teléfono: al volver a abrir la app se entra directo al Home hasta que se
use **Perfil → Cerrar sesión**.

Desde la terminal:

```powershell
.\gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk
.\gradlew installDebug           # instala en el dispositivo conectado
```

## Pruebas y cobertura

Las pruebas evaluadas son **unitarias** (JVM, sin emulador) y están en `app/src/test/java/com/example/electra/`:

| Archivo | Pruebas | Qué cubre |
|---|---:|---|
| `AutenticacionTest.kt` | 17 | Login, registro, recuperar contraseña, reglas de validación, errores del servidor y sin conexión |
| `PerfilTest.kt` | 17 | Perfil, editar nombre y foto de perfil (subida al servidor), cambiar contraseña, roles, gestión de usuarios y tema |
| `PruebasElectricasTest.kt` | 22 | Registrar prueba (validaciones, foto, GPS, clima de Open-Meteo), historial, detalle y Home |
| `ProtocolosTest.kt` | 22 | Crear, listar, formalizar y editar protocolos, permisos por rol, número del servidor, reconexión automática |
| `Apoyo.kt` | — | Datos de ejemplo y **APIs falsas** que responden como los microservicios (códigos 200, 201, 401, 404, 409, 503 o falta de red) |

```powershell
.\gradlew testDebugUnitTest              # ejecuta las 78 pruebas
.\gradlew coberturaPruebas               # + informe JaCoCo en app/build/reports/cobertura/html/index.html
.\gradlew verificarCoberturaPruebas      # falla si la cobertura baja del 80 %
```

La cobertura (91 % de líneas) se mide sobre el código lógico: `model`, `validation`, `ui/viewmodel` y
`data` (incluidos los repositorios remotos). Se excluye lo que necesita Android en ejecución: pantallas,
navegación, tema, recursos del dispositivo, `AppContainer` y `SharedPreferences`.

> Como pide la pauta, no hay pruebas de interfaz ni instrumentadas (`androidTest`): todas son unitarias.
