pluginManagement {
    includeBuild("conventions")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Also include `conventions` at the top level so it participates in DEPENDENCY substitution (the pluginManagement include above only covers plugin resolution).
// This lets `eazy-project`'s `implementation("org.eazyportal.gradle.conventions:conventions:…")` linchpin substitute to this local build
// (matched by group:name; version ignored for composite substitution) instead of resolving from a remote repository. (§5.5 / §7.2)
includeBuild("conventions")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-plugins"

include(
    "eazy-settings",
    "eazy-project",
)
