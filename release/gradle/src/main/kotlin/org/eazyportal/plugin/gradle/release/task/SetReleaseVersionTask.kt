package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.core.action.SetReleaseVersionAction
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class SetReleaseVersionTask @Inject constructor(
    private val setReleaseVersionAction: SetReleaseVersionAction<File>,
) : EazyReleaseBaseTask() {

    @TaskAction
    fun run() {
        logger.quiet("Setting release version...")

        setReleaseVersionAction.execute()
    }

}
