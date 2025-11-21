package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FinalizeSnapshotVersionTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        projectDir.initializeGitAndGradleProject()
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
