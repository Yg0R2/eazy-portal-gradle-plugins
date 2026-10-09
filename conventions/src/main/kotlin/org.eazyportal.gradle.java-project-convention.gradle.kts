import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.plugins.jvm.JvmTestSuite

/*
 * Base JVM configuration shared by every project (Java and Kotlin) — design §4.2.
 * Non-publishing. Adds NO test dependencies — those are contributed by eazy-project.
 */
plugins {
    java
    `java-test-fixtures`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)   // Java 25 toolchain → source/target 25
    }
}

// Emergency escape hatch: `-PsuppressAllErrors` demotes warnings back to warnings (default = strict). See README.
val warningsAsErrors = !providers.gradleProperty("suppressAllErrors").isPresent

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.compilerArgs.add("-Xlint:all,-preview")            // lint on; `preview` excluded (churns on JDK bumps)
    if (warningsAsErrors) options.compilerArgs.add("-Werror")  // only the promotion to error is toggleable
    // reserved for future mandatory javac args (e.g. "-parameters")
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
            }
        }

        register<JvmTestSuite>("integrationTest") {     // end-to-end cascade, hermetic
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(testFixtures(project()))
            }
        }
    }
}

// Every test source set must see everything the MAIN source set depends on — the sibling modules and their
// transitively-exposed api — so test and fixture code can use and mock the dependent modules (design §5.3; e.g. an
// `application` integration test boots Spring with every layer on the classpath). The built-in `test` suite already
// `extendsFrom implementation`; the extra suites and the test-fixtures source set don't, so wire them the same way.
// `implementation` itself `extendsFrom api`, so this one edge carries both production levels.
configurations.named("implementation").let { implementationConfiguration ->
    listOf(
        "functionalTestImplementation",
        "integrationTestImplementation",
        "testFixturesImplementation",
    ).forEach { testConfigurationName ->
        configurations.named(testConfigurationName) {
            extendsFrom(implementationConfiguration.get())
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Each suite runs INDEPENDENTLY (`./gradlew test` / `functionalTest` / `integrationTest` alone never pulls the
// others in). Under `check` they run together, ORDERED (`mustRunAfter`, not `dependsOn`); the default
// first-failure abort (no `--continue`) skips whatever hasn't started yet.
tasks.named("functionalTest") {
    mustRunAfter(
        tasks.named("test"),
    )
}
tasks.named("integrationTest") {
    mustRunAfter(
        tasks.named("test"),
        tasks.named("functionalTest"),
    )
}

tasks.named("check") {
    // `test` is already wired to `check`
    dependsOn("functionalTest", "integrationTest")
}
