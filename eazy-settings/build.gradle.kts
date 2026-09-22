/*
 * eazy-settings — Plugin<Settings> (design §6). TOOLS-68 lands the extension + the plugin's core
 * auto-apply/version-resolution behavior (§6.1/§6.3/§6.4/§6.5). Repositories/supply-chain (TOOLS-69) and the foojay
 * toolchain resolver (TOOLS-70) land in the plugin's `apply` on top of this.
 * `gradle-plugin-convention` brings gradleApi() (via java-gradle-plugin, so TestKit is on the classpath) and the jvm-test-suite structure.
 */
plugins {
    id("org.eazyportal.gradle.gradle-plugin-convention")
}

gradlePlugin {
    plugins {
        create("eazySettings") {
            id = "org.eazyportal.gradle.eazy-settings"
            implementationClass = "org.eazyportal.gradle.eazysettings.EazySettingsPlugin"
            displayName = "EazyPortal Settings Plugin"
            description = "Receiver entry point: auto-applies eazy-project to every subproject and feeds the eazyportal-core version."
        }
    }
}

dependencies {
    // reference EazyProjectPlugin/EazyProjectExtension/DefaultVersions. `eazy-project` is a sibling SUBPROJECT
    // here (not an included build like `conventions`), so it's referenced by project path, not coordinates.
    // `implementation` (not `api`): nothing compiles against eazy-settings — eazy-project only needs to be on its
    // runtime/plugin classpath, which is exactly what TestKit's `withPluginClasspath()` exposes to functional tests.
    implementation(project(":eazy-project"))
    // JDK auto-provisioning (design §6.3): applied by id in EazySettingsPlugin, so it must be on the plugin classpath.
    implementation(libs.foojay.resolver)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.platform.launcher)

    // Functional tests run real Gradle builds via TestKit (GradleRunner.withPluginClasspath()) applying
    // "org.eazyportal.gradle.eazy-settings" by bare id in synthetic receivers' settings.gradle.kts `plugins { }` block.
    functionalTestImplementation(project(":eazy-project"))
    functionalTestImplementation(testFixtures("org.eazyportal.gradle.conventions:conventions:${version}"))

    functionalTestImplementation(platform(libs.junit.bom))
    functionalTestImplementation(libs.junit.jupiter)
    functionalTestImplementation(libs.assertj.core)
    functionalTestImplementation(gradleTestKit())

    functionalTestRuntimeOnly(libs.junit.platform.launcher)

    // Integration tests apply the REAL "org.eazyportal.gradle.eazy-settings" plugin through TestKit,
    // exercising the real foojay-resolver-convention end-to-end — including its real network call
    // to api.foojay.io (design §9.4 extension, not the original lightweight wiring check).
    integrationTestImplementation(project(":eazy-project"))
    integrationTestImplementation(testFixtures("org.eazyportal.gradle.conventions:conventions:${version}"))

    integrationTestImplementation(platform(libs.junit.bom))
    integrationTestImplementation(libs.junit.jupiter)
    integrationTestImplementation(libs.assertj.core)
    integrationTestImplementation(gradleTestKit())

    integrationTestRuntimeOnly(libs.junit.platform.launcher)
}
