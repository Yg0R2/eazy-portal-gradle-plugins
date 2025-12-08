plugins {
    id("org.eazyportal.plugin.gradle.conventions.integration-test-conventions")
    id("org.eazyportal.plugin.gradle.conventions.kotlin-lib-conventions")
    id("org.eazyportal.plugin.gradle.conventions.repositories-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Test-Fixtures dependencies
    testFixturesImplementation(platform(libs.findLibrary("assertj-bom").get()))
    testFixturesImplementation(platform(libs.findLibrary("junit-bom").get()))

    testFixturesImplementation("org.assertj:assertj-core")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
    testFixturesImplementation(libs.findLibrary("kotest-jvm").get())

    // Test dependencies
    testImplementation(platform(libs.findLibrary("assertj-bom").get()))
    testImplementation(platform(libs.findLibrary("junit-bom").get()))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.findLibrary("kotest-jvm").get())
}
