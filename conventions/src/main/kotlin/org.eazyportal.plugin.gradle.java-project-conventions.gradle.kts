import org.eazyportal.plugin.gradle.convetions.libs
import org.eazyportal.plugin.gradle.convetions.version

plugins {
    idea
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

idea {
    module {
        testSources.from(sourceSets.findByName("integrationTest")?.java?.srcDirs)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
