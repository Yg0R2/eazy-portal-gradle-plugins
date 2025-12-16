package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.integration.test.testcase.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Then
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import java.io.File
import kotlin.io.path.absolutePathString

class ScmProjectThen(
    private val context: ScmProjectContext,
    private val executionResult: ExecutionResult,
) : Then<ScmProjectContext>(context) {

    fun projectVersionIn(
        projectFile: ProjectFile<File>,
        block: ObjectAssert<Version>.() -> Unit,
    ) {
        val projectVersion = context.projectActionsMap.computeIfAbsent(projectFile.getPath().absolutePathString()) {
            GradleProjectActions(projectFile)
        }.getVersion()

        block(assertThat(projectVersion))
    }

    fun scmCommitsIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(context.scmActions.getCommits(projectFile)))
    }

    fun scmStatusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(context.scmActions.status(projectFile)))
    }

    fun scmClenStatusIn(projectFile: ProjectFile<File>, branch: String) {
        scmStatusIn(projectFile) {
            containsExactly(
                "On branch $branch",
                "Your branch is up to date with '${context.scmConfig.remote}/$branch'.",
                "nothing to commit, working tree clean",
            )
        }
    }

    fun gradleTaskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

}
