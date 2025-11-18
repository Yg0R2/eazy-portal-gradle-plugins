package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.VersionComparator
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.release.core.version.model.Version
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.slf4j.LoggerFactory

class SetReleaseVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
    private val releaseVersionProvider: ReleaseVersionProvider,
    private val versionIncrementProvider: VersionIncrementProvider,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Setting release version...")

        val releaseVersion = getReleaseVersion()

        projectContext.all.forEach {
            checkoutToReleaseBranch(it.projectFile)

            it.projectActions.setVersion(releaseVersion)
        }

        LOGGER.info("Release version set to: $releaseVersion")
    }

    private fun checkoutToReleaseBranch(projectFile: ProjectFile<T>) {
        if (releaseActionContext.scmConfig.releaseBranch != releaseActionContext.scmConfig.featureBranch) {
            releaseActionContext.scmActions.checkout(projectFile, releaseActionContext.scmConfig.releaseBranch)

            releaseActionContext.scmActions.mergeNoCommit(projectFile, releaseActionContext.scmConfig.featureBranch)
        }
    }

    private fun getReleaseVersion(): Version =
        projectContext.all.asSequence().mapNotNull { (projectActions, projectFile) ->
            getVersionIncrement(projectFile)?.let {
                val currentVersion = projectActions.getVersion()

                releaseVersionProvider.provide(currentVersion, it)
            }
        }.maxWithOrNull(VersionComparator())
            ?: throw IllegalArgumentException("There are no acceptable commits.")

    private fun getVersionIncrement(projectFile: ProjectFile<T>): VersionIncrement? =
        getVersionIncrementFromScm(projectFile).let {
            if ((it == null) || (it == VersionIncrement.NONE)) {
                if (releaseActionContext.isForceRelease) {
                    VersionIncrement.PATCH
                } else {
                    null
                }
            } else {
                it
            }
        }

    private fun getVersionIncrementFromScm(projectFile: ProjectFile<T>): VersionIncrement? {
        val lastTag = runCatching {
            releaseActionContext.scmActions.getLastTag(projectFile)
        }.onFailure {
            LOGGER.warn("Ignoring missing Git tag from release version calculation.")
        }.getOrNull()

        return releaseActionContext.scmActions.getCommits(projectFile, lastTag)
            .let { versionIncrementProvider.provide(it, releaseActionContext.conventionalCommitTypes) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SetReleaseVersionAction::class.java)
    }

}
