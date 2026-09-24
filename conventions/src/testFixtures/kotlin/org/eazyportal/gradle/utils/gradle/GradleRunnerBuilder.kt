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

        internal var environment: MutableMap<String, String> = System.getenv().toMutableMap()

        /**
         * Set [withPluginClasspath] to inject the plugin-under-test classpath (needed when a synthetic project applies our convention plugins).
         * */
        var propagateClasspath: Boolean = true

        /**
         * Configures the GitHub Packages credentials from the environment and optionally redirects `GRADLE_USER_HOME` into an isolated, empty [gradleUserHome].
         */
        fun hermeticEnvironment(
            gradleUserHome: File? = null,
        ) {
            environment.remove("ORG_GRADLE_PROJECT_GitHubPackagesUsername")
            environment.remove("ORG_GRADLE_PROJECT_GitHubPackagesPassword")

            if (gradleUserHome != null) {
                environment["GRADLE_USER_HOME"] = gradleUserHome.absolutePath
            }
        }

        /**
         * Configures a stubbed environment with fake GitHub Packages credentials and optionally redirects `GRADLE_USER_HOME` into an isolated, empty [gradleUserHome].
         */
        fun stubEnvironment(
            gradleUserHome: File? = null,
        ) {
            hermeticEnvironment(gradleUserHome)

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

            val gradleRunner = GradleRunner.create()
                .withProjectDir(projectDir)
                .withEnvironment(config.environment)
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
