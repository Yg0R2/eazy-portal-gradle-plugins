package org.eazyportal.plugin.gradle.release.core.executor

import org.eazyportal.plugin.gradle.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import java.io.File
import java.util.concurrent.TimeUnit

class CommandLineExecutor : CommandExecutor<ProjectFile<File>> {

    override fun execute(projectFile: ProjectFile<File>, vararg commands: String): String {
        val process = runCatching {
            ProcessBuilder()
                .directory(projectFile.getFile())
                .command(*commands)
                .redirectErrorStream(true)
                .start()
        }.getOrElse {
            throw CliExecutionException(it.message!!, it)
        }

        val output: String = process.inputStream.use {
            it.bufferedReader()
                .readText()
                .trim()
        }

        process.waitFor(30, TimeUnit.SECONDS)

        val exitValue = process.exitValue()

        process.destroyForcibly()

        if (exitValue != 0) {
            throw CliExecutionException(output)
        }

        return output
    }

}
