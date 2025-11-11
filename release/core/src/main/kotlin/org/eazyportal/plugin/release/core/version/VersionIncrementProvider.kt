package org.eazyportal.plugin.release.core.version

import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.slf4j.LoggerFactory

class VersionIncrementProvider {

    fun provide(
        commits: List<String>,
        conventionalCommitTypes: List<ConventionalCommitType> = ConventionalCommitType.DEFAULT_TYPES
    ): VersionIncrement? =
        commits.asSequence()
            .mapNotNull { mapToCommitType(it) }
            .mapNotNull { mapToVersionIncrement(it, conventionalCommitTypes) }
            .minWithOrNull(compareBy { it.priority })

    private fun mapToCommitType(commit: String): String? {
        val commitTypeDelimiterIndex = commit.indexOf(ConventionalCommitType.TYPE_DELIMITER)

        return if (commitTypeDelimiterIndex > 1) {
            commit.take(commitTypeDelimiterIndex)
                .trim()
                .replace(ConventionalCommitType.COMMIT_TYPE_REGEX, "\${type}")
        } else {
            LOGGER.warn("Ignoring invalid commit: $commit")

            null
        }
    }

    private fun mapToVersionIncrement(
        commitType: String,
        conventionalCommitTypes: List<ConventionalCommitType>,
    ): VersionIncrement? {
        val versionIncrement = conventionalCommitTypes
            .firstOrNull { it.aliases.contains(commitType) }
            ?.versionIncrement

        return if ((versionIncrement != VersionIncrement.ERROR) && commitType.endsWith(ConventionalCommitType.BREAKING_CHANGE_INDICATOR)) {
            VersionIncrement.MAJOR
        } else {
            versionIncrement
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VersionIncrementProvider::class.java)
    }

}
