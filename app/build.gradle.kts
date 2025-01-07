plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.mobileheros.gpscamera"
    compileSdk = 34

    defaultConfig {
//        applicationId = "com.timecamera.stampcamera.gpscamera"
        applicationId = "com.watermarkpro.photostamp.timecamera"
        minSdk = 27
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("./../account2.jks")
            storePassword = "123456"
            keyAlias = "two"
            keyPassword = "123456"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.io.coil.kt.coil.compose3)

//    // The following line is optional, as the core library is included indirectly by camera-camera2
//    implementation(libs.androidx.camera.core)
//    implementation(libs.androidx.camera.camera2)
//    // If you want to additionally use the CameraX Lifecycle library
//    implementation(libs.androidx.camera.lifecycle)
//    // If you want to additionally use the CameraX VideoCapture library
//    implementation(libs.androidx.camera.video)
//    // If you want to additionally use the CameraX View class
//    implementation(libs.androidx.camera.view)

    implementation (libs.accompanist.permissions)
    implementation (libs.androidx.constraintlayout.compose)
    implementation(files("libs/capturable-3.0.0.aar"))
    implementation(files("libs/androidwm-light-0.1.2.aar"))
    implementation (libs.logger)
    implementation(libs.billing.ktx)
    implementation (libs.gson)
    implementation("com.google.dagger:hilt-android:2.44")
    implementation(libs.androidx.navigation.fragment)
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation (libs.net)
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation ("com.otaliastudios:cameraview:2.7.2")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.ultimate.bar.x)
    implementation ("com.nex3z:flow-layout:1.3.3")
    implementation (libs.glide)
    implementation (libs.eventbus)
    implementation (libs.x.permissions)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
kapt {
    correctErrorTypes = true
}
secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}