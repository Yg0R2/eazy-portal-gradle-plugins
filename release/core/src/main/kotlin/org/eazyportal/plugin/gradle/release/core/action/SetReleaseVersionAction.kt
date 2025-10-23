package org.eazyportal.plugin.gradle.release.core.action

import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.project.ProjectActionsFactory
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.gradle.release.core.version.VersionComparator
import org.eazyportal.plugin.gradle.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.gradle.release.core.version.model.Version
import org.eazyportal.plugin.gradle.release.core.version.model.VersionIncrement
import org.slf4j.LoggerFactory

class SetReleaseVersionAction<T : Any>(
    private val projectActionsFactory: ProjectActionsFactory<T>,
    private val releaseVersionProvider: ReleaseVersionProvider,
    private val versionIncrementProvider: VersionIncrementProvider,
) : ReleaseAction<T> {

    override fun execute(
        projectFile: ProjectFile<T>,
        releaseActionContext: ReleaseActionContext<T>,
    ) {
        val scmActions = releaseActionContext.scmActions()

        val projectFiles = scmActions.getSubmodules(projectFile)
            .map(projectFile::resolve)
            .let { it + projectFile }

        val releaseVersion = getReleaseVersion(releaseActionContext, projectFiles)
        val scmConfig = releaseActionContext.scmConfig()
        projectFiles.forEach {
            if (scmConfig.releaseBranch != scmConfig.featureBranch) {
                scmActions.checkout(it, scmConfig.releaseBranch)

                scmActions.mergeNoCommit(it, scmConfig.featureBranch)
            }

            projectActionsFactory.create(it)
                .setVersion(releaseVersion)
        }
    }

    private fun getReleaseVersion(
        releaseActionContext: ReleaseActionContext<T>,
        projectFiles: List<ProjectFile<T>>,
    ): Version =
        projectFiles.asSequence()
            .mapNotNull { projectFile ->
                getVersionIncrement(releaseActionContext, projectFile)?.let {
                    val currentVersion = projectActionsFactory.create(projectFile)
                        .getVersion()

                    releaseVersionProvider.provide(currentVersion, it)
                }
            }.maxWithOrNull(VersionComparator())
            ?: throw IllegalArgumentException("There are no acceptable commits.")

    private fun getVersionIncrement(
        releaseActionContext: ReleaseActionContext<T>,
        projectFile: ProjectFile<T>,
    ): VersionIncrement? =
        getVersionIncrementFromScm(releaseActionContext, projectFile).run {
            if ((this == null) || (this == VersionIncrement.NONE)) {
                if (releaseActionContext.isForceRelease()) {
                    VersionIncrement.PATCH
                } else {
                    null
                }
            } else {
                this
            }
        }

    private fun getVersionIncrementFromScm(
        releaseActionContext: ReleaseActionContext<T>,
        projectFile: ProjectFile<T>,
    ): VersionIncrement? {
        val scmActions = releaseActionContext.scmActions()

        val lastTag = runCatching {
            scmActions.getLastTag(projectFile)
        }.onFailure {
            LOGGER.warn("Ignoring missing Git tag from release version calculation.")
        }.getOrNull()

        return scmActions.getCommits(projectFile, lastTag)
            .let { versionIncrementProvider.provide(it, releaseActionContext.conventionalCommitTypes()) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SetReleaseVersionAction::class.java)
    }

}
