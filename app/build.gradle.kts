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
  id("com.android.application") version "8.13.2" // Oder die Version, die Sie verwenden möchten
  //id("org.jetbrains.kotlin.android") version "2.2.0" // Muss mit der kapt-Version übereinstimmen
  //id("org.jetbrains.kotlin.kapt") version "2.2.0" // Muss mit der kotlin-android-Version übereinstimmen
  id("org.jetbrains.kotlin.android") version "2.0.21"
  id("org.jetbrains.kotlin.kapt") version "2.0.21"
}

android {
  buildFeatures {
    viewBinding = true
  }

  // Namespace der App (Package-Name für generierten Code)
  namespace = "de.wiesenfarth.mainpegel"

  // Welche Android-SDK-Versionen zum Kompilieren genutzt werden
  compileSdk = 36

  defaultConfig {
    applicationId = "de.wiesenfarth.mainpegel" // Eindeutige App-ID

    minSdk = 31     // Android 12.0 – minimale Geräteanforderung
    targetSdk = 36  // Optimiert für Android 15

    // Versionsangaben für Google Play
    versionCode = 20251206
    versionName = "OeTTINGER V2025.12.06"

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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // Bautools-Version und NDK
  ndkVersion = "27.0.12077973"
  kotlinOptions {
    jvmTarget = "17"
  }
  buildToolsVersion = "36.1.0"
  dependenciesInfo {
    includeInBundle = true
    includeInApk = true
  }
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
  implementation(libs.core.ktx)

  // Test-Bibliotheken
  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)

  // WorkManager für Hintergrundjobs
  implementation(libs.workmanager)

  // Room Datenbank
  implementation(libs.room.runtime)

  // Annotation Processing → benötigt für Room (Java verwendet KEIN kapt)
  kapt(libs.room.compiler)

  // Optionale Room-Erweiterungen
  implementation(libs.room.ktx)     // Für Coroutines (Java kann es trotzdem nutzen)
  implementation(libs.room.paging)  // Falls Paging verwendet wird

  // MPAndroidChart – Diagramme
  implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

  // Netzwerk-Stack
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")

  // Alternative manuelle WorkManager-Version (derzeit auskommentiert)
  // implementation("androidx.work:work-runtime:2.11.0")
}
