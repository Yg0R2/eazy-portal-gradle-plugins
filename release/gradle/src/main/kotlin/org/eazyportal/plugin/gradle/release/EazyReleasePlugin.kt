package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.action.ReleaseActionContextFactory
import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.project.GradleProjectActionsFactory
import org.eazyportal.plugin.gradle.release.project.ProjectContextFactory
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask
import org.eazyportal.plugin.gradle.release.task.extension.registerReleaseActionTask
import org.eazyportal.plugin.release.core.action.*
import org.eazyportal.plugin.release.core.action.model.ReleaseActionContext
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.model.ProjectContext
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
        val releaseActionContext = ReleaseActionContextFactory.create(eazyReleaseExtension, target)

        val projectContext = ProjectContextFactory.create(
            GradleProjectActionsFactory(),
            FileSystemProjectFile(target.projectDir),
            lazy { releaseActionContext.scmActions },
        )

        val prepareRepositoryForReleaseTask = target.registerPrepareRepositoryForReleaseTask(
            projectContext,
            releaseActionContext,
        )
        val finalizeReleaseVersionTask = target.registerFinalizeReleaseVersionTask(
            projectContext,
            releaseActionContext,
        )
        val finalizeSnapshotVersionTask = target.registerFinalizeSnapshotVersionTask(
            projectContext,
            releaseActionContext,
        )
        val setReleaseVersionTask = target.registerSetReleaseVersionTask(
            projectContext,
            releaseActionContext,
        )
        val setSnapshotVersionTask = target.registerSetSnapshotVersionTask(
            projectContext,
            releaseActionContext,
        )
        val updateScmTask = target.registerUpdateScmTask(
            projectContext,
            releaseActionContext,
        )
    }

    private fun Project.registerFinalizeReleaseVersionTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>,
    ): TaskProvider<ReleaseActionTask> {
        val finalizeReleaseVersionAction = FinalizeReleaseVersionAction(
            projectContext,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            FINALIZE_RELEASE_VERSION_TASK_NAME,
            finalizeReleaseVersionAction,
        )
    }

    private fun Project.registerFinalizeSnapshotVersionTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>,
    ): TaskProvider<ReleaseActionTask> {
        val finalizeSnapshotVersionAction = FinalizeSnapshotVersionAction(
            projectContext,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            FINALIZE_SNAPSHOT_VERSION_TASK_NAME,
            finalizeSnapshotVersionAction,
        )
    }

    private fun Project.registerPrepareRepositoryForReleaseTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>,
    ): TaskProvider<ReleaseActionTask> {
        val prepareRepositoryForReleaseAction = PrepareRepositoryForReleaseAction(
            projectContext,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME,
            prepareRepositoryForReleaseAction,
        )
    }

    private fun Project.registerSetReleaseVersionTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>,
    ): TaskProvider<ReleaseActionTask> {
        val setReleaseVersionAction = SetReleaseVersionAction(
            projectContext,
            releaseActionContext,
            ReleaseVersionProvider(),
            VersionIncrementProvider(),
        )

        return tasks.registerReleaseActionTask(
            SET_RELEASE_VERSION_TASK_NAME,
            setReleaseVersionAction,
        )
    }

    private fun Project.registerSetSnapshotVersionTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>,
    ): TaskProvider<ReleaseActionTask> {
        val setSnapshotVersionAction = SetSnapshotVersionAction(
            projectContext,
            releaseActionContext,
            SnapshotVersionProvider(),
        )

        return tasks.registerReleaseActionTask(
            SET_SNAPSHOT_VERSION_TASK_NAME,
            setSnapshotVersionAction,
        )
    }

    private fun Project.registerUpdateScmTask(
        projectContext: ProjectContext<File>,
        releaseActionContext: ReleaseActionContext<File>
    ): TaskProvider<ReleaseActionTask> {
        val updateScmActions = UpdateScmAction(
            projectContext,
            releaseActionContext,
        )

        return tasks.registerReleaseActionTask(
            UPDATE_SCM_TASK_NAME,
            updateScmActions,
        )
    }

}
