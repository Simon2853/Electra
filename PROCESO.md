# Mapa de proceso de Electra

> Generado el 23/10/2026 a partir del código actual. Complementa a [ARCHITECTURE.md](ARCHITECTURE.md).
> Los nodos punteados indican pasos que existen en el código pero **no son alcanzables** hoy.

## 1. Proceso general (macro)

```mermaid
flowchart LR
    A([Técnico abre la app]) --> B[Autenticación]
    B --> C[Panel principal<br/>Home]
    C --> D[Consulta de equipos]
    C --> E[Registro de pruebas]
    C --> F[Historial de pruebas]
    C --> G[Gestión de protocolos]
    D --> E
    D --> F
    D --> G
    E -.->|se refleja en| F
    C -.-> H[Perfil / Cierre de sesión]:::na
    classDef na stroke-dasharray: 5 5,opacity:0.6
```

| Proceso | Actor | Entrada | Salida | Almacenamiento |
|---|---|---|---|---|
| Autenticación | Técnico | usuario + contraseña | acceso al Home | `SharedPreferences electra_usuarios` |
| Consulta de equipos | Técnico | código / tipo | ficha del equipo | lista fija en código |
| Registro de pruebas | Técnico | equipo, tipo, medición, resultado | fila en `pruebas` | SQLite `electra.db` |
| Historial | Técnico | código de equipo | lista de pruebas → detalle | SQLite |
| Gestión de protocolos | Técnico / responsable | equipo, responsable, estado | fila en `protocolos`, formalización | SQLite |

## 2. Autenticación y registro de usuario

```mermaid
flowchart TD
    S([Inicio]) --> L[Pantalla Login]
    L -->|"¿No tienes cuenta?"| R[Pantalla Registro]
    R --> V1{¿Campos completos?}
    V1 -- No --> E1[/"Completa todos los campos."/] --> R
    V1 -- Sí --> V2{¿Usuario sin espacios?}
    V2 -- No --> E2[/"El usuario no debe contener espacios."/] --> R
    V2 -- Sí --> V3{"¿Clave válida?<br/>8–20, mayús., minús., número, especial,<br/>sin 'electra', sin nombre/usuario"}
    V3 -- No --> E3[/Mensaje del requisito incumplido/] --> R
    V3 -- Sí --> V4{¿Confirmación coincide?}
    V4 -- No --> E4[/"Las contraseñas no coinciden."/] --> R
    V4 -- Sí --> V5{¿Usuario ya existe?}
    V5 -- Sí --> E5[/"Ese usuario ya existe."/] --> R
    V5 -- No --> G[(Guardar nombre + hash SHA-256)] --> L
    L --> I[Ingresar usuario y contraseña]
    I --> V6{¿Hash coincide?}
    V6 -- No --> E6[/"Usuario o contraseña incorrectos."/] --> L
    V6 -- Sí --> H([Home<br/>se elimina Login del back stack])
```

> La sesión **no se persiste**: en cada arranque el proceso comienza en Login.

## 3. Consulta de equipos

```mermaid
flowchart TD
    H([Home]) --> EQ[Lista de equipos<br/>5 equipos fijos]
    EQ --> F1[Buscar por código<br/>sin distinguir mayúsculas]
    EQ --> F2[Filtrar por tipo]
    F1 & F2 --> C{¿Hay resultados?}
    C -- No --> NR[/"No se encontraron equipos"/]
    C -- Sí --> SEL[Seleccionar equipo]
    SEL --> DE[Detalle del equipo]
    DE --> P1[Nueva prueba]
    DE --> P2[Ver historial]
    DE --> P3[Ver protocolos]
```

## 4. Registro de una prueba eléctrica

```mermaid
flowchart TD
    O([Home o Detalle de equipo]) --> RP[Formulario Registrar prueba<br/>equipo precargado]
    RP --> D1[Elegir equipo, tipo de prueba, unidad]
    D1 --> D2[Fecha y hora actuales<br/>editables]
    D2 --> D3[Ingresar medición, resultado, observaciones]
    D3 --> V{¿Medición vacía?}
    V -- Sí --> T1[/"Toast: Ingresa una medición"/] --> D3
    V -- No --> INS[(INSERT en pruebas)]
    INS --> OK{¿id ≠ -1?}
    OK -- No --> T2[/"Toast: No se pudo guardar la prueba"/] --> D3
    OK -- Sí --> T3[/"Toast: Prueba guardada correctamente"/] --> BACK([Volver a la pantalla anterior])
    CP[Confirmación de prueba]:::na
    classDef na stroke-dasharray: 5 5,opacity:0.6
```

## 5. Historial y detalle de pruebas

```mermaid
flowchart TD
    O([Home o Detalle de equipo]) --> HI[Historial del equipo]
    HI --> Q[(SELECT pruebas WHERE equipo = código)]
    Q --> LS[Últimas 5 pruebas del equipo<br/>más reciente primero; el contador muestra el total]
    HI --> CE[Cambiar equipo en el selector<br/>equipos con registros en BD]
    CE --> Q
    LS --> DP[Detalle de la prueba]
    DP --> Q2[(SELECT prueba por id)]
    HI -. "ON_RESUME: recarga" .-> Q
```

## 6. Gestión de protocolos

```mermaid
flowchart TD
    O([Home o Detalle de equipo]) --> LP[Lista de protocolos<br/>últimos 5 de cada equipo]
    LP --> FL[Filtrar por equipo, fecha y estado<br/>sobre los protocolos visibles]
    LP --> NP[Crear protocolo]
    NP --> N[Número sugerido = cantidad + 126]
    N --> FM[Elegir equipo, estado, responsable, observaciones]
    FM --> V{¿Número y responsable completos?}
    V -- No --> T1[/"Toast: completa el número y el responsable"/] --> FM
    V -- Sí --> INS[(INSERT en protocolos)]
    INS --> U{¿Número único?}
    U -- No --> T2[/"Toast: Número duplicado"/] --> FM
    U -- Sí --> LP
    LP --> DT[Detalle del protocolo]
    DT --> ED[Editar observación] --> UPD[(UPDATE observaciones)] --> DT
    DT --> FZ{¿Estado = Formalizado?}
    FZ -- No --> BF[Botón Formalizar] --> UPS[(UPDATE estado)] --> DT
    FZ -- Sí --> FIN([Protocolo cerrado])
```

### Ciclo de vida de un protocolo

```mermaid
stateDiagram-v2
    [*] --> RegistradoEnTerreno: crear (estado por defecto)
    [*] --> Formalizado: crear ya formalizado
    RegistradoEnTerreno --> RegistradoEnTerreno: editar observación
    RegistradoEnTerreno --> Formalizado: Formalizar Protocolo
    Formalizado --> Formalizado: editar observación
    Formalizado --> [*]
```

## 7. Cobertura de pruebas por proceso

Estado de la ejecución del 23/10/2026 en el emulador Pixel 9a, después de introducir
ViewModels y repositorios: **33 tests unitarios + 45 instrumentados, todos aprobados**.

| Proceso / paso | Test | Tipo |
|---|---|---|
| Rutas y argumentos de navegación | `navigation/RoutesTest` (5) | Unitario JVM |
| Login y Registro: lógica y todas las reglas de validación | `ui/viewmodel/AutenticacionViewModelTest` (7) | Unitario JVM |
| Registrar prueba, historial y detalle de pruebas | `ui/viewmodel/PruebasViewModelTest` (9) | Unitario JVM |
| Lista, creación y detalle de protocolos (número sugerido, duplicado, formalizar, observaciones) | `ui/viewmodel/ProtocolosViewModelTest` (11) | Unitario JVM |
| Registro: validaciones y alta en pantalla | `ui/screen/PantallasUiTest` `registro_*` (6) | UI Compose |
| Login: credenciales inválidas / válidas | `PantallasUiTest` `login_*` (2) | UI Compose |
| Home y Consulta de equipos | `PantallasUiTest` `home_*`, `equipos_*` (6) | UI Compose |
| Registrar prueba en pantalla (sin medición / guardado) | `PantallasUiTest` `registrarPrueba_*` (2) | UI Compose |
| Historial en pantalla | `PantallasUiTest.historial_*` (1) | UI Compose |
| Protocolos en pantalla: lista, formalizar, inexistente | `PantallasUiTest` `protocolos_*`, `detalleProtocolo_*` (3) | UI Compose |
| Almacenamiento de usuarios (hash, duplicados, mayúsculas, espacios) | `data/UsuariosLocalTest` (7) | Instrumentado |
| Pruebas y protocolos en SQLite + migración v1 → v2 | `data/PruebaDbHelperTest` (14) | Instrumentado SQLite |
| Registro → login → equipos → detalle | `navigation/FlujoPrincipalTest` | E2E (SQLite real aislado) |
| Login → registrar prueba → verla en el historial | `FlujoPrincipalTest` | E2E |
| Login → crear protocolo → verlo en la lista | `FlujoPrincipalTest` | E2E |

Los tests de UI usan los repositorios en memoria (`data/RepositoriosEnMemoria.kt`) inyectados en
el ViewModel. Los E2E usan `AppContainer(ContextoAislado)`, que trabaja con la base y las
preferencias reales del sistema, pero con prefijo `test_`.

### Cómo ejecutar

```powershell
# Unitarios
.\gradlew testDebugUnitTest

# Instrumentados sin desinstalar la app (conserva los datos del emulador)
.\gradlew installDebug installDebugAndroidTest
adb shell am instrument -w com.example.electra.test/androidx.test.runner.AndroidJUnitRunner
```

`connectedDebugAndroidTest` también funciona, pero desinstala la app al terminar y borra sus datos.
Los tests instrumentados usan `ContextoAislado` (`androidTest/.../TestContexto.kt`), que guarda la
base y las preferencias con prefijo `test_` y así no tocan los datos reales.
