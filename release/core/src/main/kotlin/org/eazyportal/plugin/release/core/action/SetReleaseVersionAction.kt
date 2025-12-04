package org.eazyportal.plugin.release.core.action

import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.VersionComparator
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.release.core.version.model.Version
import org.eazyportal.plugin.release.core.version.model.VersionIncrement
import org.slf4j.LoggerFactory

class SetReleaseVersionAction<T : Any>(
    private val projectContext: ProjectContext<T>,
    private val releaseActionContext: ReleaseActionContext,
    private val releaseVersionProvider: ReleaseVersionProvider,
    private val scmActions: ScmActions<T>,
    private val scmConfig: ScmConfig,
    private val versionIncrementProvider: VersionIncrementProvider,
) : ReleaseAction<T> {

    override fun execute() {
        LOGGER.info("Setting release version...")

        // When 'releaseBranch' has commits
        var releaseVersion = getReleaseVersion()

        projectContext.all.forEach { (_, projectFile) ->
            if (scmActions.getCurrentBranch(projectFile) != scmConfig.releaseBranch) {
                scmActions.checkout(projectFile, scmConfig.releaseBranch)
            }

            if (scmConfig.releaseBranch != scmConfig.featureBranch) {
                scmActions.mergeNoCommit(projectFile, scmConfig.featureBranch)
            }
        }

        // When 'featureBranch' has commits
        releaseVersion = (releaseVersion ?: getReleaseVersion())
            ?: throw IllegalArgumentException("There are no acceptable commits.")

        projectContext.all.forEach { (projectActions, _) ->
            projectActions.setVersion(releaseVersion)
        }

        LOGGER.info("Release version set to: $releaseVersion")
    }

    private fun getReleaseVersion(): Version? =
        projectContext.all.asSequence().mapNotNull { (projectActions, projectFile) ->
            getVersionIncrement(projectFile)?.let {
                val currentVersion = projectActions.getVersion()

                releaseVersionProvider.provide(currentVersion, it)
            }
        }.maxWithOrNull(VersionComparator())

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
            scmActions.getLastTag(projectFile)
        }.onFailure {
            LOGGER.warn("Ignoring missing Git tag from release version calculation.")
        }.getOrNull()

        return scmActions.getCommits(projectFile, lastTag)
            .let { versionIncrementProvider.provide(it, releaseActionContext.conventionalCommitTypes) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SetReleaseVersionAction::class.java)
    }

}
