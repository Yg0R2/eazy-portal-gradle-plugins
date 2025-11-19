package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReleaseTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
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
    }

    @CsvSource(FEATURE_BRANCH, RELEASE_BRANCH)
    @ParameterizedTest
    fun `run 'release' should fail when there are no acceptable commits`(testBranch: String) {
        // GIVEN
        gitActions.checkout(projectFile, testBranch)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                "> There are no acceptable commits.",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_001)
    }

    @Test
    fun `run 'release' from release branch should fail when there are no acceptable commits on release branch`() {
        // GIVEN
        originProjectFile.createDummyComment(FEATURE_BRANCH, "fix: dummy commit")

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                "> There are no acceptable commits.",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_001)
    }

    @Test
    fun `test 'release' from feature branch when there are acceptable commits`() {
        // GIVEN
        originProjectFile.createDummyComment(FEATURE_BRANCH, "fix: dummy commit")

        gitActions.checkout(projectFile, FEATURE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                "> Task :build",
                "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$UPDATE_SCM_TASK_NAME",
                "> Task :$RELEASE_TASK_NAME",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_002)
    }

    @Test
    fun `test 'release' from release branch when there are acceptable commits`() {
        // GIVEN
        originProjectFile.createDummyComment(RELEASE_BRANCH, "fix: dummy commit")

        gitActions.checkout(projectFile, RELEASE_BRANCH)
        gitActions.fetch(projectFile, REMOTE)

        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                "> Task :build",
                "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$UPDATE_SCM_TASK_NAME",
                "> Task :$RELEASE_TASK_NAME",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_002)
    }

    @Test
    fun `test 'release' with forceRelease`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                "> Task :build",
                "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                "> Task :$UPDATE_SCM_TASK_NAME",
                "> Task :$RELEASE_TASK_NAME",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_002)
    }

}
