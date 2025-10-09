import org.eazyportal.plugin.gradle.convetions.library
import org.eazyportal.plugin.gradle.convetions.libs

plugins {
    idea

    id("org.gradle.kotlin.kotlin-dsl")

    id("org.eazyportal.plugin.gradle.java-project-conventions")
}

repositories {
    gradlePluginPortal()
}

idea {
    module {
        sourceSets.findByName("integrationTest")?.kotlin?.srcDirs?.let {
            testSources.from(it)
        }
    }
}

dependencies {
    // Platform dependencies
    implementation(platform(libs.library("kotlinx-coroutines")))
}
