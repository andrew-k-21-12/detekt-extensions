pluginManagement {
    extra.set("detektVersion", "1.22.0")
    plugins {
        kotlin("jvm") version "1.8.0"
        id("io.gitlab.arturbosch.detekt") version extra["detektVersion"] as String
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            library("detekt-api", "io.gitlab.arturbosch.detekt:detekt-api:${extra["detektVersion"]}")
        }
    }
}

rootProject.name = "detekt-extensions"
include( // sometimes it helps to comment and uncomment it to make detekt-related updates apply
    ":sandbox",
    ":sandbox-dependency"
)
