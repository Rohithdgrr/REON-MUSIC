/*
 * REON Music App
 * Copyright (c) 2024 REON
 * 
 * This is an original, clean-room implementation.
 * No GPL-licensed code has been copied into this project.
 * All code is independently written based on publicly documented APIs.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.reon.music"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.reon.music.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // BuildConfig fields available in all flavors
        buildConfigField("String", "APP_NAME", "\"REON Music\"")
        buildConfigField("String", "LRCLIB_BASE_URL", "\"https://lrclib.net/api\"")
        buildConfigField("String", "SPONSORBLOCK_BASE_URL", "\"https://sponsor.ajay.app/api\"")
        buildConfigField("String", "RYD_BASE_URL", "\"https://returnyoutubedislikeapi.com\"")
    }
    
    signingConfigs {
        create("release") {
            val storeFileProp = project.findProperty("RELEASE_STORE_FILE") as? String
            if (storeFileProp != null) {
                storeFile = file(storeFileProp)
                storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as? String ?: ""
                keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as? String ?: ""
                keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as? String ?: ""
            }
        }
    }
    
    // Product Flavors: FOSS (no tracking) vs Full (with analytics)
    flavorDimensions += "version"
    productFlavors {
        create("foss") {
            dimension = "version"
            applicationIdSuffix = ".foss"
            versionNameSuffix = "-foss"
            
            // FOSS: No crash analytics, no tracking
            buildConfigField("boolean", "ENABLE_CRASH_ANALYTICS", "false")
            buildConfigField("boolean", "ENABLE_TRACKING", "false")
            buildConfigField("String", "SENTRY_DSN", "\"\"")
            buildConfigField("boolean", "IS_FOSS", "true")
        }
        create("full") {
            dimension = "version"
            isDefault = true
            
            // Full: With Sentry crash reporting (optional)
            buildConfigField("boolean", "ENABLE_CRASH_ANALYTICS", "true")
            buildConfigField("boolean", "ENABLE_TRACKING", "false") // Still no tracking!
            buildConfigField("String", "SENTRY_DSN", "\"https://your-sentry-dsn\"")
            buildConfigField("boolean", "IS_FOSS", "false")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
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
    
    lint {
        disable.add("MissingPermission")
        disable.add("DeprecatedSymbol")
        disable.add("ConstantConditionIf")
        disable.add("NewApi")
        disable.add("UnusedAttribute")
    }
}

dependencies {
    // Project modules
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":data:network"))
    implementation(project(":data:database"))
    implementation(project(":data:repository"))
    implementation(project(":media:playback"))
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:player"))
    implementation(project(":feature:library"))
    implementation(project(":feature:settings"))

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Coil
    implementation(libs.coil.compose)
    
    // DataStore
    implementation(libs.datastore.preferences)
    
    // WorkManager (for downloads)
    implementation(libs.workmanager)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    
    // Room (for DownloadWorker direct database access)
    implementation(libs.bundles.room)
    
    // Palette for dynamic theming
    implementation(libs.palette)
    
    // Media for Android Auto
    implementation(libs.media)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Media3 ExoPlayer
    implementation(libs.bundles.media3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

