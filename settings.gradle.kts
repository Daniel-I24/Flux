pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "flux"

include(
    ":core:model",
    ":core:domain",
    ":core:ai",
    ":core:sync",
    ":core:data",
    ":core:designsystem",
    ":mobile",
    ":wear",
)
