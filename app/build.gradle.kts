plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.armariocamara"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.armariocamara"
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

    // --- AQUÍ ESTÁ LA SOLUCIÓN A LAS ADVERTENCIAS AMARILLAS ---
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
}

dependencies {
    // Android Base
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Google ML Kit (Etiquetado automático de ropa)
    implementation("com.google.mlkit:image-labeling:17.0.7")

    // CÁMARA (CameraX)
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // BASE DE DATOS (Room)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // IMÁGENES (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // COLORES (Palette API)
    implementation("androidx.palette:palette:1.0.0")

    // IA EXTRA (TensorFlow - Opcional si usas ML Kit, pero lo dejo por si acaso)
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // TESTS
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}