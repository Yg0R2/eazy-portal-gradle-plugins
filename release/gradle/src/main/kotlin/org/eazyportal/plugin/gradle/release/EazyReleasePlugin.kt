package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.project.GradleProjectActionsFactory
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask
import org.eazyportal.plugin.gradle.release.task.extension.registerReleaseActionTask
import org.eazyportal.plugin.release.core.action.FinalizeReleaseVersionAction
import org.eazyportal.plugin.release.core.action.FinalizeSnapshotVersionAction
import org.eazyportal.plugin.release.core.action.PrepareRepositoryForReleaseAction
import org.eazyportal.plugin.release.core.action.SetReleaseVersionAction
import org.eazyportal.plugin.release.core.action.SetSnapshotVersionAction
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.model.ConventionalCommitType
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.ReleaseVersionProvider
import org.eazyportal.plugin.release.core.version.SnapshotVersionProvider
import org.eazyportal.plugin.release.core.version.VersionIncrementProvider
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

        val prepareRepositoryForReleaseTask = target.configurePrepareRepositoryForReleaseTask(
            releaseActionContext,
            projectFile,
        )
        val finalizeReleaseVersionTask = target.configureFinalizeReleaseVersionTask(
            projectActionsFactory,
            releaseActionContext,
            projectFile,
        )
        val finalizeSnapshotVersionTask = target.configureFinalizeSnapshotVersionTask(
            projectActionsFactory,
            releaseActionContext,
            projectFile,
        )
        val setReleaseVersionTask = target.configureSetReleaseVersionTask(
            projectActionsFactory,
            releaseActionContext,
            projectFile,
        )
        val setSnapshotVersionTask = target.configureSetSnapshotVersionTask(
            projectActionsFactory,
            releaseActionContext,
            projectFile,
        )
    }

    private fun Project.configureFinalizeReleaseVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        projectFile: FileSystemProjectFile,
    ): TaskProvider<ReleaseActionTask> {
        val finalizeReleaseVersionAction = FinalizeReleaseVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            FINALIZE_RELEASE_VERSION_TASK_NAME,
            finalizeReleaseVersionAction,
        )
    }

    private fun Project.configureFinalizeSnapshotVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        projectFile: FileSystemProjectFile,
    ): TaskProvider<ReleaseActionTask> {
        val finalizeSnapshotVersionAction = FinalizeSnapshotVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            FINALIZE_SNAPSHOT_VERSION_TASK_NAME,
            finalizeSnapshotVersionAction,
        )
    }

    private fun Project.configurePrepareRepositoryForReleaseTask(
        releaseActionContext: ReleaseActionContext<File>,
        projectFile: FileSystemProjectFile
    ): TaskProvider<ReleaseActionTask> {
        val prepareRepositoryForReleaseAction = PrepareRepositoryForReleaseAction(
            projectFile,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME,
            prepareRepositoryForReleaseAction,
        )
    }

    private fun Project.configureSetReleaseVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        projectFile: FileSystemProjectFile
    ): TaskProvider<ReleaseActionTask> {
        val setReleaseVersionAction = SetReleaseVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            ReleaseVersionProvider(),
            VersionIncrementProvider(),
        )

        return tasks.registerReleaseActionTask(
            SET_RELEASE_VERSION_TASK_NAME,
            setReleaseVersionAction,
        )
    }

    private fun Project.configureSetSnapshotVersionTask(
        projectActionsFactory: GradleProjectActionsFactory,
        releaseActionContext: ReleaseActionContext<File>,
        projectFile: FileSystemProjectFile
    ): TaskProvider<ReleaseActionTask> {
        val setSnapshotVersionAction = SetSnapshotVersionAction(
            projectActionsFactory,
            projectFile,
            releaseActionContext,
            SnapshotVersionProvider(),
        )

        return tasks.registerReleaseActionTask(
            SET_SNAPSHOT_VERSION_TASK_NAME,
            setSnapshotVersionAction,
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
