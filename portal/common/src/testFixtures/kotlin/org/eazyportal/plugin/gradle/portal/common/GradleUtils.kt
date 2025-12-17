package org.eazyportal.plugin.gradle.portal.common

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
//                "--stacktrace",
//                "--warning-mode=all",
                "--no-configuration-cache",
                *arguments,
            ).withPluginClasspath()
            .withProjectDir(projectDir)

}