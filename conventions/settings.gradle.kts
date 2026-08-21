import java.util.Properties

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    // Included builds do not inherit the root version catalog; read it explicitly.
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
    group = getPropertyOrElse("group") {
        rootProperties["group"]
    }.let { "$it.conventions" }

    version = getPropertyOrElse("version") {
        rootProperties["version"]
    }
}

rootProject.name = "conventions"

private fun Project.getPropertyOrElse(propertyName: String, defaultBlock: () -> Any?): String =
    findProperty(propertyName)
        ?.toString()
        ?.takeIf { it.isNotBlank() && !it.equals("unspecified", true) }
        ?: defaultBlock()?.toString()
        ?: throw IllegalStateException("Property $propertyName is not set")
