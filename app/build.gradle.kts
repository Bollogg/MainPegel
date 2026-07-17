@file:Suppress("DEPRECATION")
plugins {
  //alias(libs.plugins.android.application)
  //alias(libs.plugins.kotlin.android)
	id("com.android.application")
	id("com.google.android.gms.oss-licenses-plugin")
  // RoomDatabase
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidx.room)
}

android {
  namespace = "de.net.wiesenfarth.mainpegel"

  compileSdk = 37
  buildFeatures {
      buildConfig = true
      viewBinding = true
  }
  defaultConfig {
    applicationId = "de.net.wiesenfarth.mainpegel"
    minSdk = 32
    targetSdk = 37
    versionCode = 20260717
    versionName = "V2026.07"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    versionNameSuffix = "Keiler Dunkles"
  }
  // RoomDatabase
  room {
      schemaDirectory("$projectDir/schemas")
  }

  buildTypes {
    release {
      // Enables code-related app optimization.
      isMinifyEnabled = true

      // Enables resource shrinking.
      isShrinkResources = true

      proguardFiles(
        // Default file with automatically generated optimization rules.
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin {
    compilerOptions {
      jvmTarget.set(
        org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
      )
    }
  }
}
dependencies {

  implementation(libs.material)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // Tests
  testImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  implementation(libs.mpandroidchart)

  implementation(libs.androidx.preference)
  implementation(libs.androidx.work.ktx)

  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp.logging)

  implementation(libs.oss.licenses)

// optional
  implementation(libs.gson)}

/* ===============================
   Datenschutzerklärung Sync
   =============================== */

tasks.register<Copy>("syncPrivacyPolicy") {
	from("${rootProject.projectDir}/datenschutzerklaerung.md")
	into("$projectDir/src/main/assets")
	rename { "datenschutzerklaerung.txt" }
}

/* vor JEDEM Build ausführen */
tasks.named("preBuild") {
	dependsOn("syncPrivacyPolicy")
}
