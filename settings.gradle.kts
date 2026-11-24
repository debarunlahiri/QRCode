// Company proxy / mirror: set corporateMavenUrl (and optionally corporateMavenUser /
// corporateMavenPassword) in ~/.gradle/gradle.properties to resolve EVERYTHING through your
// internal Artifactory/Nexus instead of google(), mavenCentral() and the plugin portal.
// Leave it unset to use the public repositories directly. See gradle.properties for details.
pluginManagement {
    repositories {
        val mirrorUrl = providers.gradleProperty("corporateMavenUrl").orNull
        if (mirrorUrl != null) {
            maven {
                url = uri(mirrorUrl)
                isAllowInsecureProtocol = mirrorUrl.startsWith("http://")
                providers.gradleProperty("corporateMavenUser").orNull?.let { user ->
                    credentials {
                        username = user
                        password = providers.gradleProperty("corporateMavenPassword").orNull
                    }
                }
            }
        } else {
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
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val mirrorUrl = providers.gradleProperty("corporateMavenUrl").orNull
        if (mirrorUrl != null) {
            maven {
                url = uri(mirrorUrl)
                isAllowInsecureProtocol = mirrorUrl.startsWith("http://")
                providers.gradleProperty("corporateMavenUser").orNull?.let { user ->
                    credentials {
                        username = user
                        password = providers.gradleProperty("corporateMavenPassword").orNull
                    }
                }
            }
        } else {
            google()
            mavenCentral()
        }
    }
}

rootProject.name = "QR Code"
include(":app")
 