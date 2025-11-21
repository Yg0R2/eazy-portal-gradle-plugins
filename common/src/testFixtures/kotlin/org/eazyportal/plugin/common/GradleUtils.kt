package org.eazyportal.plugin.common

import org.gradle.testkit.runner.GradleRunner
import java.io.File

object GradleUtils {

    fun createGradleRunner(
        projectDir: File,
        vararg arguments: String,
    ): GradleRunner =
        GradleRunner.create()
            .forwardOutput()
            .withArguments(
                "--stacktrace",
                "--warning-mode=all",
//                "-Pversion=0.0.1-SNAPSHOT",
                "--no-configuration-cache",
                *arguments,
            ).withPluginClasspath()
//            .withGradleVersion("9.1.0")
            .withProjectDir(projectDir)

}
