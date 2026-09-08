/*
 * Gradle plugin modules — design §4b.2. Extends kotlin-project (toolchain, -Werror, JUnit platform, Kotlin compile
 * settings), layers `java-gradle-plugin` (plugin-marker publication, TestKit support) and publication-convention
 * (POM + repo routing), then adds a sources jar. The per-plugin `gradlePlugin { plugins { … } }` declaration stays
 * in each plugin module's own build.gradle.kts (dummy-project/dummy-settings, TOOLS-67/TOOLS-68) — out of scope here.
 */
plugins {
    id("org.eazyportal.gradle.kotlin-project-convention")
    `java-gradle-plugin`
    id("org.eazyportal.gradle.publication-convention")
}

java {
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    // `testSourceSets(...)` REPLACES the default list (normally just `test`) rather than appending to it —
    // `test` must be listed explicitly or it loses its plugin-under-test-metadata.properties resource.
    testSourceSets(sourceSets["test"], sourceSets["functionalTest"], sourceSets["integrationTest"])
}

dependencies {
    testFixturesImplementation(gradleTestKit())

    testImplementation(gradleTestKit())
}
