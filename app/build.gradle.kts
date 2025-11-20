plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.wiesenfarth.mainpegel"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "de.wiesenfarth.mainpegel"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildToolsVersion = "36.0.0"
    ndkVersion = "27.0.12077973"

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.workmanager)  // WorkManager aus Version Catalog

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler) // 🔹 Use annotationProcessor instead of kapt

    implementation(libs.room.ktx) // Optional for coroutines support
    implementation(libs.room.paging) // Optional for Paging integration

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}