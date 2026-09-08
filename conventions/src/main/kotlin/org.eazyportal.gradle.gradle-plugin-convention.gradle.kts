import org.gradle.api.plugins.jvm.JvmTestSuite

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
}

// jvm-test-suite STRUCTURE only (design §7.4) — no `libs` accessor inside a precompiled script plugin
// (gradle/gradle#15383), so no test-library dependencies are declared here. Each consuming module adds
// what it actually needs (e.g. `libs.assertj.core`) in its own real build.gradle.kts.
testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("functionalTest") {      // TestKit, hermetic
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(testFixtures(project()))
                implementation(gradleTestKit())
            }
        }

        register<JvmTestSuite>("integrationTest") {     // end-to-end cascade, hermetic
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(testFixtures(project()))
                implementation(gradleTestKit())
            }
        }
    }
}

// Each suite runs INDEPENDENTLY (`./gradlew test` / `functionalTest` / `integrationTest` alone never pulls the
// others in). Under `check` they run together, ORDERED (`mustRunAfter`, not `dependsOn`); the default
// first-failure abort (no `--continue`) skips whatever hasn't started yet.
tasks.named("functionalTest") { mustRunAfter(tasks.named("test")) }
tasks.named("integrationTest") { mustRunAfter(tasks.named("test"), tasks.named("functionalTest")) }

gradlePlugin {
    // `testSourceSets(...)` REPLACES the default list (normally just `test`) rather than appending to it —
    // `test` must be listed explicitly or it loses its plugin-under-test-metadata.properties resource.
    testSourceSets(sourceSets["test"], sourceSets["functionalTest"], sourceSets["integrationTest"])
}

tasks.named("check") { dependsOn("functionalTest", "integrationTest") }   // `test` is already wired to `check`
