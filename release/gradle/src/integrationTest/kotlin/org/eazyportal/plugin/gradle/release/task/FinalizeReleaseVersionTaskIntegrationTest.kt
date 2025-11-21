package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FinalizeReleaseVersionTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        projectDir.initializeGitAndGradleProject()
    }

    @Test
    fun `test 'run' should finalize release version`() {
        // GIVEN
        projectActions.setVersion(RELEASE_001)

        // WHEN
        val actual = createGradleRunner(projectDir, FINALIZE_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
            )
    }

    @Test
    fun `test 'run' should fail when there is nothing to commit`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, FINALIZE_RELEASE_VERSION_TASK_NAME)
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME FAILED",
                "nothing to commit, working tree clean",
            )
    }

}
