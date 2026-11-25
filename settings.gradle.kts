pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "ShelfAnything"

// App module: navigation, DI entry point, theme host.
include(":app")

// Core modules. Dependency direction: feature -> core:ui / core:domain / core:data, core:data -> core:domain.
include(":core:domain") // Pure Kotlin: models, repository interfaces, the palette and export text
include(":core:data")   // Firebase (sign-in, the collection) and the metadata sites
include(":core:ui")     // Theme and shared Compose components

// Feature modules. Features never depend on each other.
include(":feature:auth")    // Sign-in and Settings
include(":feature:shelves") // The shelves, their items, search and barcode scanning
