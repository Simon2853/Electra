plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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

    // ==========================================
    // ANDROIDX CORE
    // ==========================================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)


    // ==========================================
    // JETPACK COMPOSE
    // ==========================================

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("androidx.compose.material:material-icons-extended")


    // ==========================================
    // NAVEGACIÓN
    // ==========================================

    implementation("androidx.navigation:navigation-compose:2.9.5")


    // ==========================================
    // MVVM / LIFECYCLE
    // ==========================================

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)


    // ==========================================
    // ROOM / SQLITE
    // ==========================================

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")



    // ==========================================
    // RETROFIT / API REST
    // ==========================================

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")


    // ==========================================
    // DATASTORE
    // ==========================================

    implementation("androidx.datastore:datastore-preferences:1.1.1")


    // ==========================================
    // IMÁGENES
    // ==========================================

    implementation("io.coil-kt:coil-compose:2.7.0")


    // ==========================================
    // RESPONSIVE / WINDOW SIZE
    // ==========================================

    implementation(
        "androidx.compose.material3:material3-window-size-class:1.3.0"
    )


    // ==========================================
    // UNIT TESTS
    // ==========================================

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0"
    )


    // ==========================================
    // ANDROID TESTS
    // ==========================================

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(
        "androidx.compose.ui:ui-test-junit4"
    )


    // ==========================================
    // DEBUG / COMPOSE
    // ==========================================

    debugImplementation(libs.androidx.compose.ui.tooling)

    debugImplementation(
        "androidx.compose.ui:ui-test-manifest"
    )
}