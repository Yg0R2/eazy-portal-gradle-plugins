package org.eazyportal.gradle.conventions

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/**
 * Plain, Gradle-free logic shared by the convention plugins (design §4.5) — fast-unit-testable, no Gradle runtime needed.
 * Not part of the public API.
 */
internal object ConventionsUtils {

    /**
     * Maps a full embedded Kotlin version (e.g. `"2.3.21"`) to the matching language version (`KOTLIN_2_3`) for pinning `languageVersion`/`apiVersion` (design §4.3).
     *
     * NOTE (accepted for now): assumes Gradle's well-formed 3-part `embeddedKotlinVersion`.
     * Robust parsing (`split('.').take(2)`, malformed/2-part input) is deferred to the future proper SemVer version validation (backlog TOOLS-77, design §4.5/D13).
     */
    fun kotlinLanguageVersion(embeddedKotlinVersion: String): KotlinVersion =
        KotlinVersion.fromVersion(embeddedKotlinVersion.substringBeforeLast('.'))

    /**
     * SNAPSHOT → mavenLocal; anything else (SemVer release) → GitHub Packages. `"unspecified"`/blank is NOT a release (design §4b.1).
     *
     * NOTE (accepted for now): deliberately minimal — no case/whitespace normalization, no SemVer validation; hardening is backlog TOOLS-77 (design §4.5/D12).
     */
    fun isRelease(version: String): Boolean =
        version.isNotBlank() && version != "unspecified" && !version.endsWith("-SNAPSHOT")

}
