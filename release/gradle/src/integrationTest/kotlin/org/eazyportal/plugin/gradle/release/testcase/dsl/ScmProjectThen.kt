package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.testcase.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Then
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import java.io.File

class ScmProjectThen(
    private val executionResult: ExecutionResult,
    private val scmActions: TestScmActions<File>,
    private val scmConfig: ScmConfig,
) : Then(executionResult) {

    fun projectVersionIn(
        projectFile: ProjectFile<File>,
        expectedVersion: Version,
    ) {

    }

    fun scmCommitsIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(scmActions.getCommits(projectFile)))
    }

    fun scmStatusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(scmActions.status(projectFile)))
    }

    fun scmClenStatusIn(projectFile: ProjectFile<File>, branch: String) {
        scmStatusIn(projectFile) {
            containsExactly(
                "On branch $branch",
                "Your branch is up to date with '${scmConfig.remote}/$branch'.",
                "nothing to commit, working tree clean",
            )
        }
    }

    fun taskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

}
