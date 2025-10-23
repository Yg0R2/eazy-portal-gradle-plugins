package org.eazyportal.plugin.gradle.release.core.executor

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.gradle.release.core.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.gradle.release.core.scm.GitActions.Companion.GIT_EXECUTABLE
import org.eazyportal.plugin.gradle.release.core.utils.isWindows
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class CliCommandExecutorIntegrationTest : BaseIntegrationTest() {

    private val underTest = CommandLineExecutor()

    @Test
    fun test_execute_git() {
        // GIVEN
        // WHEN
        val actual = underTest.execute(projectFile, GIT_EXECUTABLE, "init")

        // THEN
        assertThat(actual).contains("hint: Using 'master' as the name for the initial branch.")
    }

    @Test
    fun test_execute_gradle() {
        // GIVEN
        GradleRunner.create()
            .forwardOutput()
            .withArguments("--no-configuration-cache", "init", "--dsl", "kotlin")
            .withProjectDir(projectFile.getFile())
            .build()

        // WHEN
        val actual = underTest.execute(projectFile, "./gradlew", "project")

        // THEN
        assertThat(actual).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun test_execute_shouldFail_withInvalidCommand() {
        val errorMessageSnippet = if (isWindows()) {
            "is not recognized as an internal or external command"
        } else {
            "Cannot run program"
        }

        assertThatThrownBy {
            underTest.execute(projectFile, "invalid command")
        }.isInstanceOf(CliExecutionException::class.java)
            .hasMessageContaining(errorMessageSnippet)
    }

}
