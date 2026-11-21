# Mapa de proceso de Electra

> Actualizado el 21/11/2026 (Evaluación Final). Complementa a [ARCHITECTURE.md](ARCHITECTURE.md).
> Todos los datos se guardan en los microservicios del backend
> ([Electra-Backend](https://github.com/Simon2853/Electra-Backend)); la app los consume con Retrofit
> a través del API Gateway.

## 1. Proceso general (macro)

```mermaid
flowchart LR
    A([Usuario abre la app]) --> SP[Splash Screen]
    SP --> S{¿Sesión guardada?}
    S -- No --> B[Autenticación]
    S -- Sí --> C
    B --> C[Panel principal<br/>Home]
    C --> D[Consulta de equipos]
    C --> E[Registro de pruebas]
    C --> F[Historial de pruebas]
    C --> G[Gestión de protocolos]
    C --> H[Perfil / usuarios / cierre de sesión]
    D --> E
    D --> F
    D --> G
    E -.->|se refleja en| F
```

| Proceso | Actor | Entrada | Salida | Dónde se guarda |
|---|---|---|---|---|
| Autenticación | Todos | usuario + contraseña | sesión abierta | usuarios-service (`electra_usuarios`, clave BCrypt); sesión en `SharedPreferences` |
| Consulta de equipos | Todos | código / tipo | ficha del equipo | catálogo fijo en la app |
| Registro de pruebas | Técnico / Admin | equipo, tipo, medición, resultado, foto, GPS, clima | prueba registrada | pruebas-service (`electra_pruebas`) + foto en archivos-service |
| Historial | Todos (según rol) | código de equipo | lista de pruebas → detalle | pruebas-service |
| Gestión de protocolos | Técnico crea · Supervisor formaliza | equipo, responsable, observaciones | protocolo numerado, formalización | protocolos-service (`electra_protocolos`) |
| Gestión de usuarios | Admin | usuario + rol nuevo | rol actualizado | usuarios-service |

## 2. Autenticación y registro de usuario

```mermaid
flowchart TD
    S([Inicio]) --> L[Pantalla Login]
    L -->|"¿No tienes cuenta?"| R[Pantalla Registro]
    R --> V1{"¿Formulario válido en la app?<br/>nombre, usuario 4–20 sin espacios,<br/>clave 8–20 con mayús., minús., número y especial,<br/>sin 'electra' ni datos personales, confirmación igual"}
    V1 -- No --> E1[/Mensaje bajo el campo con error/] --> R
    V1 -- Sí --> API1[POST /api/auth/registro]
    API1 --> C1{Respuesta}
    C1 -- 409 --> E2[/"Ese usuario ya existe."/] --> R
    C1 -- sin red --> E3[/Franja "Sin conexión con el servidor"/] --> R
    C1 -- 201 --> L
    L --> I[Ingresar usuario y contraseña]
    I --> API2[POST /api/auth/login]
    API2 --> C2{Respuesta}
    C2 -- 401 --> E4[/"Usuario o contraseña incorrectos."/] --> L
    C2 -- sin red --> E5[/"No se pudo conectar con el servidor. Inténtalo de nuevo."/] --> L
    C2 -- 200 --> G[(Guardar sesión en el teléfono)] --> H([Home<br/>se elimina Login del back stack])
```

> La sesión **se mantiene** al cerrar la app: al abrirla de nuevo se entra directo al Home hasta que
> el usuario use _Perfil → Cerrar sesión_.

## 3. Consulta de equipos

```mermaid
flowchart TD
    H([Home]) --> EQ[Lista de equipos<br/>catálogo fijo]
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
    RP --> D1[Elegir equipo, tipo de prueba, unidad<br/>fecha y hora actuales editables]
    D1 --> FOTO[Tomar foto de evidencia<br/>opcional]
    D1 --> GPS[Capturar ubicación GPS]
    GPS --> CLI[GET Open-Meteo<br/>temperatura, humedad, estado]
    CLI --> ADV{"¿Humedad ≥ 80 % o lluvia?"}
    ADV -- Sí --> AV[/Aviso: el ambiente puede alterar la medición<br/>botón Agregar a observaciones/]
    ADV -- No --> D3
    AV --> D3
    CLI -. "si falla, se puede registrar igual" .-> D3
    FOTO --> D3[Ingresar medición, resultado, observaciones]
    D3 --> V{"¿Válido?<br/>medición numérica 0–1.000.000,<br/>fecha no futura, motivo si es rechazada"}
    V -- No --> T1[/Error bajo el campo + sacudida + vibración/] --> D3
    V -- Sí --> UP{¿Tiene foto?}
    UP -- Sí --> SUB[POST /api/archivos<br/>archivos-service]
    SUB -- falla --> T2[/"No se pudo guardar la prueba. Inténtalo nuevamente."<br/>la foto local se conserva/] --> D3
    SUB -- ok --> INS
    UP -- No --> INS[POST /api/pruebas<br/>pruebas-service]
    INS -- falla --> T2
    INS -- 201 --> OK[Vibración de éxito] --> CP([Confirmación de la prueba<br/>Compartir respaldo])
```

## 5. Historial y detalle de pruebas

```mermaid
flowchart TD
    O([Home o Detalle de equipo]) --> HI[Historial del equipo]
    HI --> Q[GET /api/pruebas?usuario=…&equipo=…]
    Q --> SRV{{"pruebas-service filtra por rol:<br/>técnico = solo las suyas,<br/>supervisor/admin = todas"}}
    SRV --> LS[Últimas pruebas del equipo<br/>más reciente primero, comparadas con la anterior]
    HI --> CE[Cambiar equipo en el selector<br/>GET /api/pruebas/equipos]
    CE --> Q
    LS --> DP[Detalle de la prueba<br/>foto descargada con Coil]
    HI -. "ON_RESUME o al reconectar: recarga" .-> Q
```

## 6. Gestión de protocolos

```mermaid
flowchart TD
    O([Home o Detalle de equipo]) --> LP[Lista de protocolos]
    LP --> FL[Filtrar por equipo, fecha y estado]
    LP --> NP[Crear protocolo<br/>solo técnico o admin]
    NP --> N[GET /api/protocolos/siguiente-numero<br/>número referencial, solo lectura]
    N --> FM[Elegir equipo, responsable, observaciones]
    FM --> V{¿Responsable válido?}
    V -- No --> T1[/Error bajo el campo/] --> FM
    V -- Sí --> INS[POST /api/protocolos]
    INS --> SRV{{"protocolos-service asigna el número definitivo<br/>#000001, #000002… y el estado Registrado en terreno"}}
    SRV -- falla --> T2[/"No se pudo guardar. Revisa la conexión e inténtalo de nuevo."/] --> FM
    SRV -- 201 --> LP
    LP --> DT[Detalle del protocolo<br/>+ pruebas del equipo vía Feign]
    DT --> FZ{¿Formalizado?}
    FZ -- No --> ED[Editar observación<br/>PUT …/observaciones] --> DT
    FZ -- No --> BF[Formalizar<br/>solo supervisor o admin<br/>PUT …/estado] --> DT
    FZ -- Sí --> FIN([Protocolo cerrado:<br/>el servidor responde 409 a cualquier cambio])
```

### Ciclo de vida de un protocolo

```mermaid
stateDiagram-v2
    [*] --> RegistradoEnTerreno: técnico crea (número asignado por el servidor)
    RegistradoEnTerreno --> RegistradoEnTerreno: editar observación
    RegistradoEnTerreno --> Formalizado: supervisor/admin formaliza (guarda quién y cuándo)
    Formalizado --> [*]: ya no se edita ni se elimina
```

## 7. Conexión con el servidor

```mermaid
flowchart TD
    A[Cualquier llamada a un microservicio] --> B{¿Respondió?}
    B -- "Sí (2xx/4xx)" --> OK[Se usa la respuesta<br/>franja oculta]
    B -- "Sin red o 502/503/504" --> F[Franja roja<br/>Sin conexión con el servidor]
    F --> R[Reintento cada 10 s]
    R --> B
    OK -. "si antes estaba caído" .-> RC[La pantalla abierta recarga sus datos]
```

## 8. Cobertura de pruebas por proceso

Ejecución del 21/11/2026: **78 pruebas unitarias, todas aprobadas**, con **91 % de cobertura de
líneas** del código lógico (JaCoCo). Se ejecutan en la JVM con APIs falsas que responden como los microservicios.

| Proceso / paso | Archivo de pruebas |
|---|---|
| Login, registro, recuperar contraseña, reglas de validación, 401/409 y sin red | `AutenticacionTest` (17) |
| Perfil, editar nombre y foto (subida al servidor), cambiar contraseña, roles, gestión de usuarios, tema | `PerfilTest` (17) |
| Registrar prueba (validaciones, foto, GPS, clima Open-Meteo), historial, comparación, detalle, Home | `PruebasElectricasTest` (22) |
| Crear, listar, formalizar y editar protocolos, permisos, número del servidor, 409 en formalizados, reconexión automática | `ProtocolosTest` (22) |

### Cómo ejecutar

```powershell
.\gradlew testDebugUnitTest              # pruebas unitarias
.\gradlew coberturaPruebas               # + informe en app/build/reports/cobertura/html/index.html
.\gradlew verificarCoberturaPruebas      # falla si la cobertura baja del 80 %
```
