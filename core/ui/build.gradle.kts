plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.compose)
}

android {
    namespace = "com.kokouliss.shelfanything.core.ui"
}

dependencies {
    api(project(":core:domain"))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui)
    api(libs.coil.compose)
    implementation(libs.androidx.core)
}
