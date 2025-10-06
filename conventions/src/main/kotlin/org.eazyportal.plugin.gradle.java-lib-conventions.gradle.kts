 plugins {
    id("java-library")

    id("org.eazyportal.plugin.gradle.java-project-conventions")
    id("org.eazyportal.plugin.gradle.publish-conventions")
}

repositories {
    gradlePluginPortal()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }
}
