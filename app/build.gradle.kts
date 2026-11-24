import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlinx.kover")
}

// Firma del APK release: se lee de keystore.properties (en la raíz del proyecto).
val keystoreProps = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

fun propiedad(nombre: String, defecto: String = ""): String =
    (project.findProperty(nombre) as String?) ?: defecto

android {
    namespace = "cl.aquaxor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.aquaxor.app"
        minSdk = 26
        targetSdk = 35
        versionCode = propiedad("AQUAXOR_VERSION_CODE", "2").toInt()
        versionName = propiedad("AQUAXOR_VERSION_NAME", "1.1.0")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Backend InsForge (opcional). Vacío = la sincronización avisa que falta configurar.
        buildConfigField("String", "INSFORGE_URL", "\"${propiedad("INSFORGE_URL")}\"")
        buildConfigField("String", "INSFORGE_KEY", "\"${propiedad("INSFORGE_KEY")}\"")
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all { it.useJUnitPlatform() } // pruebas unitarias con JUnit 5
        }
    }
}

// Reporte de cobertura (Kover). Se excluye lo que solo se prueba en dispositivo:
// pantallas Compose, Room (DAO y clases generadas) y envoltorios del framework Android.
kover {
    reports {
        filters {
            excludes {
                classes(
                    "cl.aquaxor.app.ui.*",
                    "cl.aquaxor.app.data.local.*",
                    "cl.aquaxor.app.data.storage.*",
                    "cl.aquaxor.app.data.network.ConnectivityObserver*",
                    "cl.aquaxor.app.AquaXorApp",
                    "cl.aquaxor.app.MainActivity",
                    "cl.aquaxor.app.AppContainer",
                    "cl.aquaxor.app.BuildConfig",
                    "*ComposableSingletons*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Room (SQLite)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Carga de imágenes desde archivos locales
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit: API de clima (Open-Meteo) y backend InsForge
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---- Pruebas unitarias (JUnit 5) ----
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("io.mockk:mockk:1.13.12")

    // ---- Pruebas de interfaz (Compose UI Test; el runner de AndroidX usa JUnit 4) ----
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
