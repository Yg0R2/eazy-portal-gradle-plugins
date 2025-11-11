package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.VersionComparator
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.release.core.version.model.Version
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.slf4j.LoggerFactory

class SetReleaseVersionAction<T : Any>(
    private val projectActionsFactory: ProjectActionsFactory<T>,
    private val projectFile: ProjectFile<T>,
    private val releaseActionContext: ReleaseActionContext<T>,
    private val releaseVersionProvider: ReleaseVersionProvider,
    private val versionIncrementProvider: VersionIncrementProvider,
) : ReleaseAction<T>(
    projectFile,
    releaseActionContext,
) {

    override fun execute() {
        LOGGER.info("Setting release version...")

        val releaseVersion = getReleaseVersion()

        allProjectFiles.forEach {
            checkoutToReleaseBranch(it)

            projectActionsFactory.create(it)
                .setVersion(releaseVersion)
        }

        LOGGER.info("Release version set to: $releaseVersion")
    }

    private fun checkoutToReleaseBranch(projectFile: ProjectFile<T>) {
        if (scmConfig.releaseBranch != scmConfig.featureBranch) {
            scmActions.checkout(projectFile, scmConfig.releaseBranch)

            scmActions.mergeNoCommit(projectFile, scmConfig.featureBranch)
        }
    }

    private fun getReleaseVersion(): Version =
        allProjectFiles.asSequence().mapNotNull { projectFile ->
            getVersionIncrement(projectFile)?.let {
                val currentVersion = projectActionsFactory.create(projectFile)
                    .getVersion()

                releaseVersionProvider.provide(currentVersion, it)
            }
        }.maxWithOrNull(VersionComparator())
            ?: throw IllegalArgumentException("There are no acceptable commits.")

    private fun getVersionIncrement(projectFile: ProjectFile<T>): VersionIncrement? =
        getVersionIncrementFromScm(projectFile).let {
            if ((it == null) || (it == VersionIncrement.NONE)) {
                if (releaseActionContext.isForceReleaseProvider()) {
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
            scmActions.getLastTag(projectFile)
        }.onFailure {
            LOGGER.warn("Ignoring missing Git tag from release version calculation.")
        }.getOrNull()

        return scmActions.getCommits(projectFile, lastTag)
            .let { versionIncrementProvider.provide(it, releaseActionContext.conventionalCommitTypesProvider()) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SetReleaseVersionAction::class.java)
    }

}
