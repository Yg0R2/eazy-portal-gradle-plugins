pluginManagement {
    includeBuild("conventions")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-plugins"

include(
    "dummy-settings",
    "eazy-project",
)
