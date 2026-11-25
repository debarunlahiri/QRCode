// Fully offline: every plugin and dependency is resolved from the ./offline-repo folder
// that lives inside this project. No network access / company proxy is needed.
// To add or upgrade a dependency, drop its Maven layout (pom/module/jar/aar) into offline-repo.
val offlineRepo = uri("${rootDir}/offline-repo")

pluginManagement {
    repositories {
        maven {
            url = uri("${settings.rootDir}/offline-repo")
            metadataSources { gradleMetadata(); mavenPom(); artifact() }
        }
        // Fallback for anything not yet vendored in offline-repo.
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = offlineRepo
            metadataSources { gradleMetadata(); mavenPom(); artifact() }
        }
        // Fallback for anything not yet vendored in offline-repo.
        google()
        mavenCentral()
    }
}

rootProject.name = "QR Code"
include(":app")
