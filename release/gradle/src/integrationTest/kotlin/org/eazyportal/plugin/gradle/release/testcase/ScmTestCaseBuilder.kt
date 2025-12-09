package org.eazyportal.plugin.gradle.release.testcase

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.testcase.BaseProjectTestCase
import org.eazyportal.plugin.common.integration.test.testcase.Then
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.gradle.testkit.runner.BuildResult
import java.io.File

class ScmAssert(
    private val testCase: BaseScmProjectTestCase,
) : Then.Assert<BaseProjectTestCase>(testCase) {

    fun commitsIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(testCase.scmActions.getCommits(projectFile)))
    }

    fun statusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(testCase.scmActions.status(projectFile)))
    }

    fun statusCleanIn(
        projectFile: ProjectFile<File>,
        branch: String,
    ) {
        statusIn(projectFile) {
            containsExactly(
                "On branch $branch",
                "Your branch is up to date with '${testCase.scmConfig.remote}/$branch'.",
                "nothing to commit, working tree clean",
            )
        }
    }

}
