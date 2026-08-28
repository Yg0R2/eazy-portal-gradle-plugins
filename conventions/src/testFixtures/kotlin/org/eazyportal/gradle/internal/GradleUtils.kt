package org.eazyportal.gradle.internal

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/**
 * Shared helpers for the TestKit tests that drive the `conventions` build itself.
 * Only broadly reusable pieces live here — test-specific expectations (asserted values, repo layout of a given test) stay in the tests.
 */
object GradleUtils {

    /**
     * Runs a failing Gradle build and returns its output.
     *
     * Set [withPluginClasspath] to inject the plugin-under-test classpath (needed when a synthetic project applies our convention plugins).
     */
    fun runFailingGradleTask(
        projectDir: File,
        vararg arguments: String,
        withPluginClasspath: Boolean = false,
    ): String =
        createGradleRunner(projectDir, arguments, withPluginClasspath)
            .buildAndFail()
            .output
            .also { assertThat(it).isNotBlank }

    /**
     * Runs a successful Gradle build and returns its output.
     *
     * Set [withPluginClasspath] to inject the plugin-under-test classpath (needed when a synthetic project applies our convention plugins).
     */
    fun runGradleTask(
        projectDir: File,
        vararg arguments: String,
        withPluginClasspath: Boolean = false,
    ): String =
        createGradleRunner(projectDir, arguments, withPluginClasspath)
            .build()
            .output
            .also { assertThat(it).isNotBlank }

    private fun createGradleRunner(
        projectDir: File,
        arguments: Array<out String>,
        withPluginClasspath: Boolean,
    ): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*arguments)
            .forwardOutput()
            .apply {
                if (withPluginClasspath) {
                    withPluginClasspath()
                }
            }

}
