package org.eazyportal.plugin.gradle.release.action

import org.eazyportal.plugin.gradle.release.extension.getOrElse
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.api.Project
import java.io.File

object ReleaseActionContextFactory {

    fun create(
        eazyReleaseExtension: EazyReleasePluginExtension,
        target: Project,
    ): ReleaseActionContext<File> =
        object : ReleaseActionContext<File>() {

            override val conventionalCommitTypes: List<ConventionalCommitType> by lazy {
                eazyReleaseExtension.conventionalCommitTypes
                    .get()
                    .ifEmpty { ConventionalCommitType.Companion.DEFAULT_TYPES }
            }

            override val isForceRelease: Boolean by lazy {
                target.providers
                    .systemProperty("forceRelease")
                    .orNull
                    .toBoolean()
            }

            override val scmActions: ScmActions<File> by lazy {
                eazyReleaseExtension.scmActions
                    .getOrElse { GitActions(CommandLineExecutor()) }
            }

            override val scmConfig: ScmConfig by lazy {
                eazyReleaseExtension.scmConfig
                    .orNull
                    ?: run {
                        val releaseBranch = target.providers
                            .systemProperty("releaseBranch")
                            .getOrElse { ScmConfig.GIT_FLOW.releaseBranch }

                        val featureBranch = target.providers
                            .systemProperty("featureBranch")
                            .getOrElse {
                                scmActions.getCurrentBranch(FileSystemProjectFile(target.projectDir))
                            }

                        ScmConfig(featureBranch, releaseBranch, ScmConstants.REMOTE)
                    }
            }

        }

}
