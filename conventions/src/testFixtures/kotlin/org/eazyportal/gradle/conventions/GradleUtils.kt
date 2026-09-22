package org.eazyportal.gradle.conventions

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
     * Set [environment] to isolate/override the forked daemon's environment (e.g. redirect `GRADLE_USER_HOME` and strip ambient
     * `ORG_GRADLE_PROJECT_*` credentials) — omit it for tests that don't touch credential-gated repositories.
     */
    fun runFailingGradleTask(
        projectDir: File,
        vararg arguments: String,
        withPluginClasspath: Boolean = true,
        environment: Map<String, String>? = null,
    ): String =
        createGradleRunner(projectDir, arguments, withPluginClasspath, environment)
            .buildAndFail()
            .output
            .also {
                assertThat(it)
                    .isNotBlank
                    .contains("BUILD FAILED")
            }

    /**
     * Runs a successful Gradle build and returns its output.
     *
     * Set [withPluginClasspath] to inject the plugin-under-test classpath (needed when a synthetic project applies our convention plugins).
     * Set [environment] to isolate/override the forked daemon's environment (e.g. redirect `GRADLE_USER_HOME` and strip ambient
     * `ORG_GRADLE_PROJECT_*` credentials) — omit it for tests that don't touch credential-gated repositories.
     */
    fun runGradleTask(
        projectDir: File,
        vararg arguments: String,
        withPluginClasspath: Boolean = true,
        environment: Map<String, String>? = null,
    ): String =
        createGradleRunner(projectDir, arguments, withPluginClasspath, environment)
            .build()
            .output
            .also {
                assertThat(it)
                    .isNotBlank
                    .contains("BUILD SUCCESSFUL")
            }

    private fun createGradleRunner(
        projectDir: File,
        arguments: Array<out String>,
        withPluginClasspath: Boolean,
        environment: Map<String, String>?,
    ): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*arguments)
            .withEnvironment(environment)
            .forwardOutput()
            .apply {
                if (withPluginClasspath) {
                    withPluginClasspath()
                }
            }

}
