import java.util.Properties

plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.kokouliss.shelfanything.core.data"
    buildFeatures.buildConfig = true

    // Keys for the three sites that need one (TMDB for movies, RAWG for video games, BoardGameGeek
    // for board games). They come from local.properties on your own computer and from GitHub secrets
    // in the workflow; without them those shelves say that the key is missing. See "Metadata sites" in the README.
    val keys = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    fun key(name: String) = (keys.getProperty(name) ?: System.getenv(name)).orEmpty()
    defaultConfig {
        buildConfigField("String", "TMDB_API_KEY", "\"${key("TMDB_API_KEY")}\"")
        buildConfigField("String", "RAWG_API_KEY", "\"${key("RAWG_API_KEY")}\"")
        buildConfigField("String", "BGG_API_TOKEN", "\"${key("BGG_API_TOKEN")}\"")
    }
}

dependencies {
    api(project(":core:domain"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization)
    implementation(libs.okhttp)

    testImplementation(libs.bundles.unit.test)
}
