plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.chandra.practice.deviceinfo"
    compileSdk = 37

    signingConfigs {
        create("release") {
            keyAlias = "android"
            keyPassword = "device1234"
            storeFile = file("C:\\Users\\balachandra.d\\private\\DeviceInfo\\keystore\\keystore.jks")
            storePassword = "device1234"
        }
    }

    defaultConfig {
        applicationId = "com.chandra.practice.deviceinfo"
        minSdk = 27
        targetSdk = 36
        versionCode = 8
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            // Bundles native debug symbols so Play Console can symbolicate native crashes and ANRs.
            ndk {
                debugSymbolLevel = "FULL"
            }
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                         )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            // Optional: you can specify debug signing config here if needed
        }
    }

    // With AGP 9 built-in Kotlin, the Kotlin jvmTarget follows these compileOptions.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.windowsizeclass)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
