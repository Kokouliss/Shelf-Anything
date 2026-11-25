plugins {
    alias(libs.plugins.app.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kokouliss.shelfanything.feature.shelves"
}

dependencies {
    // Google's own barcode sheet: no camera permission and no camera code in the app.
    implementation(libs.google.play.services.code.scanner)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization)

    testImplementation(libs.bundles.unit.test)
}
