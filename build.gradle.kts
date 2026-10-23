// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // 👇 Aquí es donde Gradle aprende qué versión de KSP usar
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}