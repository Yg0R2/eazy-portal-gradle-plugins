package org.eazyportal.plugin.gradle.release.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.integration.test.dsl.TestScenario
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import java.io.File
import kotlin.io.path.pathString

abstract class BaseScmProjectThen(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    private val projectActionsMap: MutableMap<String, ProjectActions<File>>,
    private val executionResult: TestScenario.ExecutionResult,
) : ScmProjectThen {

    //------------------------------------
    // Gradle
    //------------------------------------

    override fun gradleTaskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

    //------------------------------------
    // Project
    //------------------------------------

    override fun projectVersionIn(
        projectFile: ProjectFile<File>,
        block: ObjectAssert<Version>.() -> Unit,
    ) {
        val projectVersion = projectActionsMap.computeIfAbsent(projectFile.getPath().pathString) {
            GradleProjectActions(projectFile)
        }.getVersion()

        block(
            assertThat(projectVersion)
        )
    }

    //------------------------------------
    // SCM Commit
    //------------------------------------

    override fun scmCommitsIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(
            assertThat(
                scmActions.getCommits(projectFile)
            )
        )
    }

    override fun scmCompareCommitsIn(
        left: ProjectFile<File>,
        right: ProjectFile<File>,
        alsoAssertBlock: ListAssert<String>.() -> Unit,
    ) {
        assertThat(scmActions.getCommits(left))
            .containsExactlyElementsOf(scmActions.getCommits(right))
            .alsoAssertBlock()
    }

    //------------------------------------
    // SCM Status
    //------------------------------------

    override fun scmClenStatusIn(
        projectFile: ProjectFile<File>,
        branch: String,
    ) {
        scmStatusIn(projectFile) {
            containsExactly(
                "On branch $branch",
                "Your branch is up to date with '${scmConfig.remote}/$branch'.",
                "nothing to commit, working tree clean",
            )
        }
    }

    override fun scmStatusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(
            assertThat(
                scmActions.status(projectFile)
            )
        )
    }

}
