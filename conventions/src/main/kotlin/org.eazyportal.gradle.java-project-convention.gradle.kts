import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

/*
 * Base JVM configuration shared by every project (Java and Kotlin) — design §4.2.
 * Non-publishing. Adds NO test dependencies — those are contributed by dummy-project.
 */
plugins {
    java
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()           // JUnit Platform enabled here; JUnit deps come from dummy-project
}
