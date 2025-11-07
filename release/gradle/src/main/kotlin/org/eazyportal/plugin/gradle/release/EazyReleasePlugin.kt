package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.core.action.SetReleaseVersionAction
import org.eazyportal.plugin.gradle.release.core.action.SetSnapshotVersionAction
import org.eazyportal.plugin.gradle.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.gradle.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.gradle.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.gradle.release.core.scm.GitActions
import org.eazyportal.plugin.gradle.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.gradle.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.gradle.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.gradle.release.core.version.SnapshotVersionProvider
import org.eazyportal.plugin.gradle.release.core.version.VersionIncrementProvider
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.project.GradleProjectActionsFactory
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import java.io.File

class EazyReleasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val eazyReleaseExtension = target.extensions.create<EazyReleasePluginExtension>("eazyRelease")
        val releaseActionContext = createReleaseActionContext(eazyReleaseExtension, target)

        val projectActionsFactory = GradleProjectActionsFactory()
        val projectFile = FileSystemProjectFile(target.projectDir)

        val setReleaseVersionTask = configureSetReleaseVersionTask(
            projectActionsFactory,
            releaseActionContext,
            target,
            projectFile,
        )
        val setSnapshotVersionTask = configureSetSnapshotVersionTask(
            projectActionsFactory,
            releaseActionContext,
            target,
            projectFile,
        )
    }

    private fun configureSetReleaseVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        project: Project,
        projectFile: FileSystemProjectFile
    ): TaskProvider<ReleaseActionTask> {
        val setReleaseVersionAction = SetReleaseVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            ReleaseVersionProvider(),
            VersionIncrementProvider(),
        )

        return project.tasks.register(
            SET_RELEASE_VERSION_TASK_NAME,
            ReleaseActionTask::class.java,
            setReleaseVersionAction,
            SET_RELEASE_VERSION_TASK_NAME
        )
    }

    private fun configureSetSnapshotVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        project: Project,
        projectFile: FileSystemProjectFile
    ): TaskProvider<ReleaseActionTask> {
        val setSnapshotVersionAction = SetSnapshotVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            SnapshotVersionProvider(),
        )

        return project.tasks.register(
            SET_SNAPSHOT_VERSION_TASK_NAME,
            ReleaseActionTask::class.java,
            setSnapshotVersionAction,
            SET_SNAPSHOT_VERSION_TASK_NAME,
        )
    }

    private fun createReleaseActionContext(
        eazyReleaseExtension: EazyReleasePluginExtension,
        target: Project
    ): ReleaseActionContext<File> = ReleaseActionContext(
        conventionalCommitTypesProvider = {
            eazyReleaseExtension.conventionalCommitTypes
                .get()
                .ifEmpty { ConventionalCommitType.DEFAULT_TYPES }
        },
        isForceReleaseProvider = {
            target.providers
                .systemProperty("forceRelease")
                .orNull
                .toBoolean()
        },
        scmActionsProvider = {
            eazyReleaseExtension.scmActions
                .getOrElse(GitActions(CommandLineExecutor()))
        },
        scmConfigProvider = {
            eazyReleaseExtension.scmConfig
                .getOrElse(ScmConfig.GIT_FLOW)
        }
    )

}
