import java.util.Properties

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    // Included builds do not inherit the root version catalig; read it explicitly.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

// Included builds do not inherit the root gradle.properties; read it explicitly.
// A command-line -P (e.g. -Pversion=X.Y.Z at release) still wins via findProperty.
val rootProperties = Properties().apply {
    file("../gradle.properties").inputStream().use { load(it) }
}
gradle.rootProject {
    group = "${(findProperty("group") ?: rootProperties["group"])}.conventions"
    version = (findProperty("version") ?: rootProperties["version"]).toString()
}

rootProject.name = "conventions"
