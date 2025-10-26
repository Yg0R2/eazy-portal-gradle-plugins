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
        sourceSets.findByName("integrationTest")?.java?.srcDirs?.run {
            testSources.setFrom(this)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
