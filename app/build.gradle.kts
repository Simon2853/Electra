plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

android {
    namespace = "com.example.electra"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.electra"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // JaCoCo registra qué líneas ejecutan las pruebas unitarias (ver tarea coberturaPruebas al final)
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Splash Screen oficial de Android (funciona igual desde Android 7 hasta las versiones nuevas)
    implementation("androidx.core:core-splashscreen:1.2.0")

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.9.5")

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation("junit:junit:4.13.2")
    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0"
    )

    debugImplementation(libs.androidx.compose.ui.tooling)

    // NUEVO (Retrofit): cliente HTTP que convierte una interfaz Kotlin en llamadas a la API REST
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    // NUEVO (Retrofit): convierte JSON <-> data class de Kotlin usando Gson
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    // NUEVO (Corrutinas): trae viewModelScope para lanzar las llamadas de red desde el ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // NUEVO (Foto en servidor): Coil descarga y muestra imágenes desde una URL (AsyncImage)
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")
}

// ---------------------------------------------------------------------------------------------
// COBERTURA DE PRUEBAS (JaCoCo)
//   .\gradlew coberturaPruebas            -> informe en app/build/reports/cobertura/html/index.html
//   .\gradlew verificarCoberturaPruebas   -> falla si el código lógico tiene menos de 80 % de líneas probadas
//
// Se mide el "código lógico": modelos, validaciones, ViewModels y repositorios (incluido el acceso
// a los microservicios). Se excluye lo que no se puede probar con pruebas unitarias en la JVM porque
// depende de Android en ejecución: pantallas Compose, navegación, tema, cámara/GPS/vibración y
// SharedPreferences. Esas partes se revisan en el emulador.
// ---------------------------------------------------------------------------------------------

jacoco {
    toolVersion = "0.8.14"
}

val clasesLogicas = fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
    include(
        "com/example/electra/model/**",
        "com/example/electra/validation/**",
        "com/example/electra/ui/viewmodel/**",
        "com/example/electra/data/**"
    )
    exclude(
        "com/example/electra/data/AppContainer*",      // solo conecta piezas; necesita Context
        "com/example/electra/data/*Preferencias*"      // SharedPreferences de Android
    )
}
val datosCobertura = layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")

tasks.register<JacocoReport>("coberturaPruebas") {
    group = "verification"
    description = "Ejecuta las pruebas unitarias y genera el informe de cobertura del código lógico."
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(clasesLogicas)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(datosCobertura)
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/cobertura/html"))
        csv.required.set(true)
        csv.outputLocation.set(layout.buildDirectory.file("reports/cobertura/cobertura.csv"))
    }
}

tasks.register<JacocoCoverageVerification>("verificarCoberturaPruebas") {
    group = "verification"
    description = "Falla si las pruebas unitarias cubren menos del 80 % de las líneas del código lógico."
    dependsOn("coberturaPruebas")
    classDirectories.setFrom(clasesLogicas)
    executionData.setFrom(datosCobertura)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
