plugins {
    idea

    id("org.gradle.kotlin.kotlin-dsl")

    id("org.eazyportal.plugin.gradle.conventions.java-project-conventions")
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
    // Test dependencies
    testImplementation("io.mockk:mockk:1.14.6")
}
