package org.eazyportal.plugin.gradle.release.core.executor

import org.eazyportal.plugin.gradle.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile

interface CommandExecutor<T: ProjectFile<*>> {

    @Throws(CliExecutionException::class)
    fun execute(projectFile: T, vararg commands: String): String

}
