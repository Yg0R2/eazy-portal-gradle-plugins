package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.core.action.SetReleaseVersionAction
import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.gradle.release.core.scm.GitActions
import org.eazyportal.plugin.gradle.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.gradle.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.gradle.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.project.GradleProjectActionsFactory
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.SetReleaseVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import java.io.File

class EazyReleasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val eazyReleaseExtension = target.extensions.create<EazyReleasePluginExtension>("eazyRelease")

        val releaseActionContext = createReleaseActionContext(eazyReleaseExtension, target)

        val setReleaseVersionTask = configureSetReleaseVersionTask(releaseActionContext, target)
    }

    private fun configureSetReleaseVersionTask(
        releaseActionContext: ReleaseActionContext<File>,
        project: Project,
    ): TaskProvider<SetReleaseVersionTask> {
        val setReleaseVersionAction = SetReleaseVersionAction(
            GradleProjectActionsFactory(),
            ReleaseVersionProvider(),
            VersionIncrementProvider()
        )

        return project.tasks.register(
            SET_RELEASE_VERSION_TASK_NAME,
            SetReleaseVersionTask::class.java,
            setReleaseVersionAction
        ).apply {
            configure {
                this.releaseActionContext.set(releaseActionContext)
            }
        }
    }

    private fun createReleaseActionContext(
        eazyReleaseExtension: EazyReleasePluginExtension,
        target: Project
    ): ReleaseActionContext<File> = ReleaseActionContext(
        conventionalCommitTypes = {
            eazyReleaseExtension.conventionalCommitTypes
                .getOrElse(ConventionalCommitType.DEFAULT_TYPES)
        },
        isForceRelease = {
            target.providers
                .systemProperty("forceRelease")
                .orNull
                .toBoolean()
        },
        scmActions = {
            eazyReleaseExtension.scmActions
                .getOrElse(GitActions(CommandLineExecutor()))
        },
        scmConfig = {
            eazyReleaseExtension.scmConfig
                .getOrElse(ScmConfig.GIT_FLOW)
        }
    )

}
