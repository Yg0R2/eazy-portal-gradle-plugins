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

dependencies {
    // Test-Fixtures dependencies
    testFixturesImplementation(platform("org.assertj:assertj-bom:3.27.6"))
    testFixturesImplementation(platform("org.junit:junit-bom:6.0.0"))

    testFixturesImplementation("org.assertj:assertj-core")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")

    // Test dependencies
    testImplementation(platform("org.assertj:assertj-bom:3.27.6"))
    testImplementation(platform("org.junit:junit-bom:6.0.0"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
