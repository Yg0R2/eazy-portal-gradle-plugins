package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.core.action.SetSnapshotVersionAction
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class SetSnapshotVersionTask @Inject constructor(
    private val setSnapshotVersionAction: SetSnapshotVersionAction<File>
) : EazyReleaseBaseTask() {

    @TaskAction
    fun run() {
        logger.quiet("Setting SNAPSHOT version...")

        setSnapshotVersionAction.execute()
    }

}