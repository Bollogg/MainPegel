plugins {
    //alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android)
	id("com.android.application")
	id("com.google.android.gms.oss-licenses-plugin")
	id("org.jetbrains.kotlin.android")
    // RoomDatabase
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)


}

android {
    namespace = "de.net.wiesenfarth.mainpegel"
    compileSdk {
        version = release(36)
    }
    // Version-Name (versionName) in InfoActivity freigeben
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "de.net.wiesenfarth.mainpegel"
        minSdk = 32
        targetSdk = 36
        versionCode = 20260206
        versionName = "OeTTINGER V2026.02"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // RoomDatabase V2.8.x
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

    buildToolsVersion = "36.1.0"
}

dependencies {
	implementation("androidx.preference:preference:1.2.1")
  val workVersion = "2.11.1"
  implementation("androidx.work:work-runtime-ktx:$workVersion")
  implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  // RoomDatabase
  implementation(libs.androidx.room.runtime)
  // Retrofit für API-Abfragen
  implementation("com.squareup.retrofit2:retrofit:3.0.0")
  // Converter für JSON (meistens Gson oder Moshi)
  implementation("com.squareup.retrofit2:converter-gson:3.0.0")
  implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
	implementation("com.google.android.gms:play-services-oss-licenses:17.3.0")
	implementation("com.google.code.gson:gson:2.10.1")
}

