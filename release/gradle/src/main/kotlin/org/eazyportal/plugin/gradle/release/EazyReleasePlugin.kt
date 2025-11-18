package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.model.EazyReleasePluginExtension
import org.eazyportal.plugin.gradle.release.task.*
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask.Companion.RELEASE_TASKS_GROUP
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class EazyReleasePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // TODO: validate root project
        target.extensions.create<EazyReleasePluginExtension>("eazyRelease")

        val setReleaseVersionTask = target.tasks
            .register<SetReleaseVersionTask>(SET_RELEASE_VERSION_TASK_NAME) {
                mustRunAfter("clean")
            }

        val finalizeReleaseVersionTask = target.tasks
            .register<FinalizeReleaseVersionTask>(FINALIZE_RELEASE_VERSION_TASK_NAME) {
                mustRunAfter(SET_RELEASE_VERSION_TASK_NAME)
            }

        target.tasks.named("build").configure {
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
