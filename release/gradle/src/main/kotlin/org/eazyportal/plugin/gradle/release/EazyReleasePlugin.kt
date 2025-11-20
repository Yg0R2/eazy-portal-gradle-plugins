package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.FinalizeReleaseVersionTask
import org.eazyportal.plugin.gradle.release.task.FinalizeSnapshotVersionTask
import org.eazyportal.plugin.gradle.release.task.PrepareRepositoryForReleaseTask
import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask.Companion.RELEASE_TASKS_GROUP
import org.eazyportal.plugin.gradle.release.task.SetReleaseVersionTask
import org.eazyportal.plugin.gradle.release.task.SetSnapshotVersionTask
import org.eazyportal.plugin.gradle.release.task.UpdateScmTask
import org.eazyportal.plugin.release.core.project.exception.ProjectException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class EazyReleasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target != target.rootProject) {
            throw ProjectException("Plugin can be applied only to the root project.")
        }

        target.extensions.create<EazyReleasePluginExtension>("eazyRelease")

        val prepareRepositoryForReleaseTask = target.tasks
            .register<PrepareRepositoryForReleaseTask>(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME) {
                mustRunAfter("clean")
            }

        val setReleaseVersionTask = target.tasks
            .register<SetReleaseVersionTask>(SET_RELEASE_VERSION_TASK_NAME) {
                mustRunAfter(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            }

        val finalizeReleaseVersionTask = target.tasks
            .register<FinalizeReleaseVersionTask>(FINALIZE_RELEASE_VERSION_TASK_NAME) {
                mustRunAfter(SET_RELEASE_VERSION_TASK_NAME)
            }

        target.tasks.named("build") {
            mustRunAfter(
                SET_RELEASE_VERSION_TASK_NAME,
                FINALIZE_RELEASE_VERSION_TASK_NAME,
            )
        }

        val setSnapshotVersionTask = target.tasks
            .register<SetSnapshotVersionTask>(SET_SNAPSHOT_VERSION_TASK_NAME) {
                mustRunAfter("build")
            }

        val finalizeSnapshotVersionTask = target.tasks
            .register<FinalizeSnapshotVersionTask>(FINALIZE_SNAPSHOT_VERSION_TASK_NAME) {
                mustRunAfter(SET_SNAPSHOT_VERSION_TASK_NAME)
            }

        val updateScmTask = target.tasks
            .register<UpdateScmTask>(UPDATE_SCM_TASK_NAME) {
                mustRunAfter(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            }

        val releaseTask = target.tasks.register(RELEASE_TASK_NAME) {
            group = RELEASE_TASKS_GROUP

            dependsOn(
                prepareRepositoryForReleaseTask,
                setReleaseVersionTask,
                finalizeReleaseVersionTask,
                "build", // releaseBuildTasks,
                setSnapshotVersionTask,
                finalizeSnapshotVersionTask,
                updateScmTask,
            )
        }
    }

}
