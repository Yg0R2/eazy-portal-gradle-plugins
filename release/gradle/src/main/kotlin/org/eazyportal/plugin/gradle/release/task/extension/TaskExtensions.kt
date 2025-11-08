package org.eazyportal.plugin.gradle.release.task.extension

import org.eazyportal.plugin.gradle.release.task.ReleaseActionTask
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun TaskContainer.registerReleaseActionTask(
    taskName: String,
    vararg constructorArgs: Any,
): TaskProvider<ReleaseActionTask> =
    register(
        taskName,
        ReleaseActionTask::class.java,
        *constructorArgs,
        taskName
    )
