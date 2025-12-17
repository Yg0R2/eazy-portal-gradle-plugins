package org.eazyportal.plugin.gradle.release.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.integration.test.testcase.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import java.io.File
import kotlin.io.path.absolutePathString

class ScmProjectThen<C : ScmProjectTestContext>(
    private val context: C,
    private val executionResult: ExecutionResult,
) : Then<C>(context) {

    val scmActions: TestScmActions<File> = context.scmActions
    val scmConfig: ScmConfig = context.scmConfig

    fun projectVersionIn(
        projectFileProvider: C.() -> ProjectFile<File> = { projectDir.localProjectFile },
        block: ObjectAssert<Version>.() -> Unit,
    ) {
        val projectFile = projectFileProvider(context)

        val projectVersion = context.projectActionsMap
            .computeIfAbsent(projectFile.getPath().absolutePathString()) {
                GradleProjectActions(projectFile)
            }.getVersion()

        block(assertThat(projectVersion))
    }

    fun scmCommitsIn(
        projectFileProvider: C.() -> ProjectFile<File> = { projectDir.localProjectFile },
        block: ListAssert<String>.() -> Unit,
    ) {
        block(
            assertThat(
                context.scmActions.getCommits(
                    projectFileProvider(context)
                )
            )
        )
    }

    fun scmStatusIn(
        projectFileProvider: C.() -> ProjectFile<File> = { projectDir.localProjectFile },
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(context.scmActions.status(projectFileProvider(context))))
    }

    fun scmClenStatusIn(
        projectFileProvider: C.() -> ProjectFile<File> = { projectDir.localProjectFile },
        branch: String,
    ) {
        scmStatusIn(projectFileProvider) {
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
