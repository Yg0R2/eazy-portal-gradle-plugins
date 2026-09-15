/*
 * eazy-project — Plugin<Project> (design §5).
 * TOOLS-65/TOOLS-66 land the archetype model + the Configurer hierarchy; TOOLS-67 lands the plugin class
 * (EazyProjectPlugin), its `gradlePlugin { plugins { create("eazyProject") } }` block, and the functional tests.
 * `gradle-plugin-convention` brings gradleApi() (via java-gradle-plugin, so ProjectBuilder is on the classpath) and the jvm-test-suite structure.
 */
plugins {
    id("org.eazyportal.gradle.gradle-plugin-convention")
}

gradlePlugin {
    plugins {
        create("eazyProject") {
            id = "org.eazyportal.gradle.eazy-project"
            implementationClass = "org.eazyportal.gradle.eazyproject.EazyProjectPlugin"
            displayName = "Dummy Project"
            description = "Configures a receiver module's archetype-based conventions, dependencies, and eazy-portal-core wiring."
        }
    }
}

dependencies {
    // LINCHPIN (§5.5): the conventions plugins must be on this module's classpath so a Configurer can pluginManager.apply("org.eazyportal.gradle.kotlin-*-convention").
    // Locally this substitutes to the `conventions` included build (auto-substituted by matching group:name — the version in the notation is ignored for composite substitution).
    // `conventions` is an INCLUDED BUILD, not a subproject, so it is referenced by its coordinates (group `org.eazyportal.gradle.conventions`), never `project(":conventions")`.
    // `implementation` (not `api`) is correct: nothing compiles against eazy-project — the conventions only need to
    // be on its runtime/plugin classpath. At receivers this resolves from GitHub Packages.
    implementation("org.eazyportal.gradle.conventions:conventions:${version}")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.platform.launcher)

    // Functional tests run real Gradle builds via TestKit (GradleRunner.withPluginClasspath()) applying
    // "org.eazyportal.gradle.eazy-project" by bare id to synthetic receiver modules.
    functionalTestImplementation(testFixtures("org.eazyportal.gradle.conventions:conventions:${version}"))

    functionalTestImplementation(platform(libs.junit.bom))
    functionalTestImplementation(libs.junit.jupiter)
    functionalTestImplementation(libs.assertj.core)
    functionalTestImplementation(gradleTestKit())

    functionalTestRuntimeOnly(libs.junit.platform.launcher)
}
