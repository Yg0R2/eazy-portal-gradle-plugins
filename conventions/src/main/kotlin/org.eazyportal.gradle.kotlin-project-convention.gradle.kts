import org.eazyportal.gradle.conventions.ConventionsUtils
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Kotlin(JVM) projects — design §4.3. Extends java-project (toolchain, -Werror, JUnit platform),
 * then layers Kotlin compile settings. No Java sources expected, but Java config still applies.
 */
plugins {
    id("org.eazyportal.gradle.java-project-convention")
    kotlin("jvm")                          // version tied to embeddedKotlinVersion via the plugin classpath
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        // Emergency escape hatch: `-PsuppressAllErrors` demotes warnings back to warnings (default = strict). See README.
        allWarningsAsErrors = providers.gradleProperty("suppressAllErrors").map { false }.orElse(true)

        // Pin language/api to the embedded Kotlin: a newer Kotlin requires a newer Gradle,
        // which forces a new release of these plugins (design §1/§4.3).
        val embedded = ConventionsUtils.kotlinLanguageVersion(embeddedKotlinVersion)  // "2.3.21" → KOTLIN_2_3
        languageVersion = embedded
        apiVersion = embedded
        // reserved for future mandatory Kotlin args (e.g. "-Xjsr305=strict")
    }
}
