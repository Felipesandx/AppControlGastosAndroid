plugins {
    alias(libs.plugins.android.application)
    id("com.google.devtools.ksp") // Solo KSP y Android
}

android {
    namespace = "edu.umg.appcontrolgastos"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.umg.appcontrolgastos"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 1. Tu librería local
    implementation(files("libs/app-gastos-core-1.0.0.jar"))

    // 2. Room (Persistencia SQLite nativa)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // 3. UI y AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}