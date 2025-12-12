package org.eazyportal.plugin.release.core.executor

import org.eazyportal.plugin.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.release.core.project.ProjectFile

interface CommandExecutor<in T: ProjectFile<*>> {

    @Throws(CliExecutionException::class)
    fun execute(projectFile: T, vararg commands: String): List<String>

}
