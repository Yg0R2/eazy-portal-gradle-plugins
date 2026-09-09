/*
 * eazy-project — Plugin<Project> (design §5). The plugin class + configurers land in TOOLS-67; this
 * module only needs to compile/test the archetype model (ProjectType/DefaultVersions/EazyProjectExtension,
 * TOOLS-65) for now. `gradle-plugin-convention` already brings gradleApi() (via java-gradle-plugin) and the
 * jvm-test-suite structure (test/functionalTest/integrationTest).
 */
plugins {
    id("org.eazyportal.gradle.gradle-plugin-convention")
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.platform.launcher)
}
