plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.citygo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.citygo.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read from local.properties
        val localProperties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        buildConfigField("String", "BACKEND_BASE_URL",      "\"${localProperties.getProperty("BACKEND_BASE_URL", "http://10.0.2.2:8000/")}\"")
        buildConfigField("String", "ADMIN_API_KEY",         "\"${localProperties.getProperty("ADMIN_API_KEY", "citygo-admin-key-2024")}\"")
        buildConfigField("String", "OPENTRIPMAP_API_KEY",   "\"${localProperties.getProperty("OPENTRIPMAP_API_KEY", "")}\"")
        buildConfigField("String", "OPEN_METEO_BASE_URL",   "\"https://api.open-meteo.com/\"")
        buildConfigField("String", "PHOTON_BASE_URL",       "\"https://photon.komoot.io/\"")
        buildConfigField("String", "OSRM_BASE_URL",         "\"https://router.project-osrm.org/\"")
        buildConfigField("String", "OPENTRIPMAP_BASE_URL",  "\"https://api.opentripmap.com/0.1/\"")
        buildConfigField("String", "WIKIPEDIA_BASE_URL",    "\"https://en.wikipedia.org/api/rest_v1/\"")
        buildConfigField("String", "OVERPASS_API_URL",      "\"https://overpass-api.de/api/interpreter\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.location)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.fonts)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)

    // Maps - osmdroid
    implementation(libs.osmdroid)

    // DataStore
    implementation(libs.datastore.prefs)

    // Splash screen
    implementation(libs.splashscreen)

    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)

    // Shimmer
    implementation(libs.shimmer)

    // Timber
    implementation(libs.timber)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // WorkManager
    implementation(libs.workmanager)

    // Lottie
    implementation(libs.lottie.compose)

    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
}
