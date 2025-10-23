package org.eazyportal.plugin.gradle.release.core.version

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class VersionIncrementProviderTest {

    private val underTest = VersionIncrementProvider()

    @MethodSource(
        "majorVersionIncrements",
        "minorVersionIncrements",
        "patchVersionIncrements",
        "noneVersionIncrement",
        "nullVersionIncrement",
        "customConventionalCommitTypes"
    )
    @ParameterizedTest
    fun test_provide(
        commits: List<String>,
        conventionalCommitTypes: List<ConventionalCommitType>,
        expected: VersionIncrement?
    ) {
        // GIVEN
        // WHEN
        val actual = underTest.provide(commits, conventionalCommitTypes)

        // THEN
        assertThat(actual).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun majorVersionIncrements(): List<Arguments> =
            listOf(
                listOf("feature!: message", "test: message", "invalid: message"),
                listOf("feature: message", "test: message", "invalid!: message"),
                listOf("fix!: message", "test: message", "invalid: message"),
                listOf("fix: message", "test: message", "invalid!: message"),
                listOf("invalid(scope)!: message"),
                listOf("invalid[ticket-id]!: message"),
                listOf("invalid(scope)[ticket-id]!: message"),
            ).map { Arguments.of(it, ConventionalCommitType.DEFAULT_TYPES, VersionIncrement.MAJOR) }

        @JvmStatic
        private fun minorVersionIncrements(): List<Arguments> =
            listOf(
                listOf("feat: message", "test: message", "invalid: message"),
                listOf("invalid: message", "test: message", "feat: message"),
                listOf("feature: message", "test: message", "invalid: message"),
                listOf("invalid: message", "test: message", "feature: message"),
                listOf("feature(scope): message"),
                listOf("feature[ticket-id]: message"),
                listOf("feature(scope)[ticket-id]: message"),
            ).map { Arguments.of(it, ConventionalCommitType.DEFAULT_TYPES, VersionIncrement.MINOR) }

        @JvmStatic
        private fun patchVersionIncrements(): List<Arguments> =
            listOf(
                listOf("fix: message", "test: message", "invalid: message"),
                listOf("invalid: message", "test: message", "fix: message"),
                listOf("refactor: message", "test: message", "invalid: message"),
                listOf("invalid: message", "test: message", "refactor: message"),
                listOf("style: message", "test: message", "invalid: message"),
                listOf("invalid: message", "test: message", "style: message"),
                listOf("fix(scope): message"),
                listOf("fix[ticket-id]: message"),
                listOf("fix(scope)[ticket-id]: message"),
            ).map { Arguments.of(it, ConventionalCommitType.DEFAULT_TYPES, VersionIncrement.PATCH) }

        @JvmStatic
        private fun noneVersionIncrement(): List<Arguments> =
            listOf(
                listOf("build: message", "invalid: message"),
                listOf("chore: message", "invalid: message"),
                listOf("docs: message", "invalid: message"),
                listOf("ci: message", "invalid: message"),
                listOf("test: message", "invalid: message"),
                listOf("build(scope): message"),
                listOf("build[ticket-id]: message"),
                listOf("build(scope)[ticket-id]: message"),
            ).map { Arguments.of(it, ConventionalCommitType.DEFAULT_TYPES, VersionIncrement.NONE) }

        @JvmStatic
        private fun nullVersionIncrement(): List<Arguments> =
            listOf(
                listOf(),
                listOf("custom: message"),
                listOf("invalid commit message"),
            ).map { Arguments.of(it, ConventionalCommitType.DEFAULT_TYPES, null) }

        @JvmStatic
        private fun customConventionalCommitTypes(): List<Arguments> =
            listOf(
                emptyList(),
                listOf(ConventionalCommitType(listOf("test"), VersionIncrement.NONE))
            ).map { Arguments.of(listOf("feature: message"), it, null) }
    }

}
