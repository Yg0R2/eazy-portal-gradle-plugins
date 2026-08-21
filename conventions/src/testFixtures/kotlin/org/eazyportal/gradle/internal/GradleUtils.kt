package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/**
 * Shared helpers for the TestKit tests that drive the `conventions` build itself.
 * Only broadly reusable pieces live here — test-specific expectations (asserted values, repo layout of a given test) stay in the tests.
 */
object GradleUtils {

    /** Runs a failing Gradle build and returns its output. */
    fun runFailingGradleTask(
        projectDir: File,
        vararg arguments: String,
    ): String =
        createGradleRunner(projectDir, *arguments)
            .buildAndFail()
            .output
            .also { assertThat(it).isNotBlank }

    /** Runs a successful Gradle build and returns its output. */
    fun runGradleTask(
        projectDir: File,
        vararg arguments: String,
    ): String =
        createGradleRunner(projectDir, *arguments)
            .build()
            .output
            .also { assertThat(it).isNotBlank }

    private fun createGradleRunner(
        projectDir: File,
        vararg arguments: String,
    ): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*arguments)
            .forwardOutput()

}
