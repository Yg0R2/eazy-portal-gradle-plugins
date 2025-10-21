plugins {
    id("org.eazyportal.plugin.gradle.integration-test-conventions")
    id("org.eazyportal.plugin.gradle.kotlin-lib-conventions")
    id("org.eazyportal.plugin.gradle.repositories-conventions")
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
