package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.project.GradleProjectConstants.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.scm.ScmConstants.MAIN_BRANCH
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FinalizeSnapshotVersionTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeAll
    fun initialize() {
        projectDir.initializeGitAndGradleProject()

        projectDir.copyIntoFromResources(GRADLE_PROPERTIES_FILE_NAME)

        gitActions.add(projectFile, "*")
        gitActions.commit(projectFile, "chore: add gradle.properties")
    }

    @BeforeEach
    fun setUp() {
        gitActions.checkout(projectFile, MAIN_BRANCH)
    }

    @AfterEach
    fun tearDown() {
        gitActions.execute(projectFile, "reset", "--hard")
    }

    @Test
    fun `test 'run' should finalize snapshot version`() {
        // GIVEN
        projectActions.setVersion(SNAPSHOT_002)

        // WHEN
        val actual = createGradleRunner(projectDir, FINALIZE_SNAPSHOT_VERSION_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
            )
    }

    @Test
    fun `test 'run' should fail when there is nothing to commit`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME FAILED",
                "nothing to commit, working tree clean",
            )
    }

}
