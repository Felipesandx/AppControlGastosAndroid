plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp") // KSP en el módulo app
}

android {
    namespace = "edu.umg.appcontrolgastos"
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.umg.appcontrolgastos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true //  Conexión directa con los XML
    }
}

dependencies {
    // 1. Tu librería creada en IntelliJ
    implementation("umg.edu.gt.progra2:gastos-core:1.0.0")

    // 2. Room (Persistencia SQLite nativa)
    val roomVersion = "2.6.1" // o "2.8.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // 3. Componentes de UI y Lifecycle
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}