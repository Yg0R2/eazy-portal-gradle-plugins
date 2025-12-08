package org.eazyportal.plugin.common.cli

import java.io.File
import java.util.concurrent.TimeUnit

object CommandLineUtils {
    fun File.execute(vararg commands: String): String {
        val process = runCatching {
            ProcessBuilder()
                .directory(this)
                .command(*commands)
                .redirectErrorStream(true)
                .start()
        }.getOrElse {
            throw RuntimeException(it.message!!, it)
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
            throw RuntimeException(output)
        }

        return output
    }

    fun File.git(vararg commands: String): String =
        execute(GIT_EXECUTABLE, *commands)

    private val GIT_EXECUTABLE =
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "git.exe"
        } else {
            "git"
        }

}