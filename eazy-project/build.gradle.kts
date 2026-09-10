/*
 * eazy-project — Plugin<Project> (design §5).
 * TOOLS-66 lands the Configurer hierarchy (ProjectConfigurer / GradleProjectConfigurer / the 8 concrete configurers / ProjectConfigurers factory / WiringSummary, §5.4);
 * TOOLS-65 already landed the archetype model (ProjectType/DefaultVersions/EazyProjectExtension).
 * The plugin class (EazyProjectPlugin), the `gradlePlugin { plugins { create("eazyProject") } }` block, and the runtime form of the conventions linchpin still land in TOOLS-67.
 * `gradle-plugin-convention` brings gradleApi() (via java-gradle-plugin, so ProjectBuilder is on the classpath) and the jvm-test-suite structure.
 */
plugins {
    id("org.eazyportal.gradle.gradle-plugin-convention")
}

dependencies {
    // LINCHPIN (§5.5): the conventions plugins must be on this module's classpath so a Configurer can pluginManager.apply("org.eazyportal.gradle.kotlin-*-convention").
    // Locally this substitutes to the `conventions` included build (auto-substituted by matching group:name — the version in the notation is ignored for composite substitution).
    // `conventions` is an INCLUDED BUILD, not a subproject, so it is referenced by its coordinates (group `org.eazyportal.gradle.conventions`), never `project(":conventions")`.
    // The runtime rationale (travel to receivers via GitHub Packages) + the gradlePlugin block are finalized in TOOLS-67.
    implementation("org.eazyportal.gradle.conventions:conventions:${version}")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.platform.launcher)
}
