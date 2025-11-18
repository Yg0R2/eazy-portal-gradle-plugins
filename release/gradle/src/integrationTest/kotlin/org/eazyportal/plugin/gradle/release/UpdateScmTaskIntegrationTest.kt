package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@TestMethodOrder(OrderAnnotation::class)
class UpdateScmTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        originProjectDir.initializeGitAndGradleProject()
        // Workaround for using none-bare repository
        gitActions.execute(originProjectFile, "config", "receive.denyCurrentBranch", "ignore")

        gitActions.execute(
            FileSystemProjectFile(workingDir),
            "-c",
            "protocol.file.allow=always",
            "clone",
            "--recurse-submodules",
            originProjectDir.resolve(".git").path,
            PROJECT_NAME
        )

        // Branch is cloned, but needs to be created locally
        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.checkout(projectFile, RELEASE_BRANCH)
    }

    @AfterEach
    fun tearDown() {
        gitActions.execute(projectFile, "reset", "--hard")
    }

    @CsvSource(RELEASE_BRANCH, FEATURE_BRANCH)
    @ParameterizedTest
    fun `test 'run' should update SCM with commits`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        projectFile.createDummyComment(testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, UPDATE_SCM_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$UPDATE_SCM_TASK_NAME"
            )

        // Workaround for using none-bare repository
        gitActions.execute(originProjectFile, "reset", "--hard")
        gitActions.checkout(originProjectFile, testBranch)

        assertThat(gitActions.getCommits(projectFile))
            .containsExactlyElementsOf(gitActions.getCommits(originProjectFile))
    }

}
