package org.eazyportal.plugin.gradle.release.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.gradle.release.testcase.dsl.ScmProjectContext
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import java.io.File

class ScmProjectThen(
    val scmActions: TestScmActions<File>,
    val scmConfig: ScmConfig,
    val projectDir: ProjectDir,
    private val executionResult: TestScenario.ExecutionResult,
) : Then {

    fun gradleTaskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

    fun scmStatusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(scmActions.status(projectFile)))
    }

}
