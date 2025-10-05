import org.eazyportal.plugin.gradle.convetions.libs
import org.eazyportal.plugin.gradle.convetions.version

plugins {
    java
    `java-test-fixtures`
}

repositories {
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.version("java"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
