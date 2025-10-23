package org.eazyportal.plugin.gradle.release.core.scm.model

import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement

data class ConventionalCommitType(
    val aliases: List<String>,
    val versionIncrement: VersionIncrement,
) {

    init {
        if (aliases.isEmpty()) {
            throw IllegalArgumentException("Required to have at least 1 alias for each type.")
        }
    }

    companion object {
        val COMMIT_TYPE_REGEX = Regex("^(?<type>\\w*+)(?:\\((?<scope>.*?)\\))?(?:\\[(?<ticket>.*?)\\])?$")

        const val BREAKING_CHANGE_INDICATOR = '!'
        const val TYPE_DELIMITER = ':'

        val DEFAULT_TYPES = listOf(
            ConventionalCommitType(listOf("BREAKING CHANGE"), VersionIncrement.MAJOR),

            ConventionalCommitType(listOf("feat", "feature"), VersionIncrement.MINOR), // implement new feature

            ConventionalCommitType(listOf("fix"), VersionIncrement.PATCH), // add bugfix
            ConventionalCommitType(listOf("refactor"), VersionIncrement.PATCH), // refactoring implementation
            ConventionalCommitType(listOf("style"), VersionIncrement.PATCH), // code formatting

            ConventionalCommitType(listOf("build"), VersionIncrement.NONE), // change build system
            ConventionalCommitType(listOf("chore"), VersionIncrement.NONE), // does not change src or test
            ConventionalCommitType(listOf("docs"), VersionIncrement.NONE), // add/update documentation
            ConventionalCommitType(listOf("ci"), VersionIncrement.NONE), // CI
            ConventionalCommitType(listOf("test"), VersionIncrement.NONE) // add/fix/update test
        )
    }

}
