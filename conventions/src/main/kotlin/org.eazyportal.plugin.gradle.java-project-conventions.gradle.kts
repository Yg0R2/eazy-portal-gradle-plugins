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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

idea {
    module {
        sourceSets.findByName("integrationTest")?.java?.srcDirs?.let {
            testSources.from(it)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
