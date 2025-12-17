/*******************************************************
 * Datei:      build.gradle.kts (Module-Level)
 *
 * Beschreibung:
 *  Gradle-Konfiguration der Android-App
 *  Definiert SDK-Versionen, Build-Typen und alle Abhängigkeiten.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-28
 *******************************************************/

plugins {
    // Plugin für Android-Apps (aus Versionskatalog)
    alias(libs.plugins.android.application)
}

android {

    // Namespace der App (Package-Name für generierten Code)
    namespace = "de.wiesenfarth.mainpegel"

    // Welche Android-SDK-Versionen zum Kompilieren genutzt werden
    compileSdk {
        version = release(36)   // SDK 36 (Android 15)
    }

    defaultConfig {
        applicationId = "de.wiesenfarth.mainpegel" // Eindeutige App-ID

        minSdk = 31     // Android 12.0 – minimale Geräteanforderung
        targetSdk = 36  // Optimiert für Android 15

        // Versionsangaben für Google Play
        versionCode = 20251205
        versionName = "OeTTINGER V2025.12.05"

        // Test Runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {
            // Code wird im Release-Build nicht optimiert (Debug-freundlich)
            isMinifyEnabled = false

            // Regeln für Code-Optimierung / Obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java-Version für Android-Projekt
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Bautools-Version und NDK
    buildToolsVersion = "36.0.0"
    ndkVersion = "27.0.12077973"
}

dependencies {

    // Standard AndroidX UI-Komponenten
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Test-Bibliotheken
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // WorkManager für Hintergrundjobs
    implementation(libs.workmanager)

    // Room Datenbank
    implementation(libs.room.runtime)

    // Annotation Processing → benötigt für Room (Java verwendet KEIN kapt)
    annotationProcessor(libs.room.compiler)

    // Optionale Room-Erweiterungen
    implementation(libs.room.ktx)     // Für Coroutines (Java kann es trotzdem nutzen)
    implementation(libs.room.paging)  // Falls Paging verwendet wird

    // MPAndroidChart – Diagramme
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Netzwerk-Stack
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Alternative manuelle WorkManager-Version (derzeit auskommentiert)
    // implementation("androidx.work:work-runtime:2.11.0")
}
