package org.eazyportal.gradle.utils.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/**
 * Shared helpers for the TestKit tests that drive the `conventions` build itself.
 * Only broadly reusable pieces live here — test-specific expectations (asserted values, repo layout of a given test) stay in the tests.
 */
@GradleRunnerDsl
class GradleRunnerBuilder private constructor(
    private val gradleRunner: GradleRunner,
) {

    /**
     *  Runs a failing Gradle build and returns its output.
     */
    fun runFailingGradleTask(
        vararg arguments: String,
    ): String =
        gradleRunner
            .withArguments(*arguments)
            .buildAndFail()
            .output
            .also {
                assertThat(it)
                    .isNotBlank
                    .contains("BUILD FAILED")
            }

    /**
     * Runs a successful Gradle build and returns its output.
     */
    fun runGradleTask(
        vararg arguments: String,
    ): String =
        gradleRunner
            .withArguments(*arguments)
            .build()
            .output
            .also {
                assertThat(it)
                    .isNotBlank
                    .contains("BUILD SUCCESSFUL")
            }

    @GradleRunnerDsl
    class ConfigBuilder {

        internal var environment: MutableMap<String, String> = mutableMapOf()

        /**
         * Sets the `GRADLE_USER_HOME`.
         */
        var gradleUserHome: File? = null

        /**
         * Set [withPluginClasspath] to inject the plugin-under-test classpath (needed when a synthetic project applies our convention plugins).
         */
        var propagateClasspath: Boolean = true

        /**
         * Sets or overwrites environment variables for the GradleRunner
         */
        fun environment(
            key: String,
            value: String,
        ) {
            environment[key] = value
        }

        /**
         * Sets fake GitHub Packages credentials environment variables.
         */
        fun stubGithubCredentials() {
            environment["ORG_GRADLE_PROJECT_GitHubPackagesUsername"] = "stub-user"
            environment["ORG_GRADLE_PROJECT_GitHubPackagesPassword"] = "stub-token"
        }

    }

    companion object {
        fun gradleRunner(
            projectDir: File,
            configure: ConfigBuilder.() -> Unit = {},
        ): GradleRunnerBuilder {
            val config = ConfigBuilder().apply(configure)

            val environment = config.environment.apply {
                config.gradleUserHome?.run {
                    put("GRADLE_USER_HOME", this.absolutePath)
                }
            }

            val gradleRunner = GradleRunner.create()
                .withProjectDir(projectDir)
                .withEnvironment(environment)
                .forwardOutput()
                .apply {
                    if (config.propagateClasspath) {
                        withPluginClasspath()
                    }
                }

            return GradleRunnerBuilder(gradleRunner)
        }
    }

}
