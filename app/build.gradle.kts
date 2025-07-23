plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.chandra.practice.deviceinfo"
    compileSdk = 35

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
        minSdk = 24
        targetSdk = 35 // align with compileSdk
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            //shrinkResources = true  // uncommented to reduce APK size in release builds
            isDebuggable = false
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
