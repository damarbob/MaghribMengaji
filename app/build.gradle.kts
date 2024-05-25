plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.simsinfotekno.maghribmengaji"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.simsinfotekno.maghribmengaji"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.play.services.mlkit.document.scanner)
    implementation(libs.androidx.activity)
    implementation(libs.play.services.code.scanner)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
//    implementation(libs.firebase.storage.ktx)
    annotationProcessor(libs.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")

    //
    implementation(libs.glide)
    implementation(libs.android.document.filter) // Add document filter to bitmap
    implementation("com.google.code.gson:gson:2.10.1") // Serialization/deserialization dependency
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("org.greenrobot:eventbus:3.3.1") // Event bus
}