package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.gradle.release.BaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_010
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_100
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetReleaseVersionTaskIntegrationTest : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        projectDir.initializeGitAndGradleProject()

        gitActions.checkout(projectFile, FEATURE_BRANCH)
    }

    @Test
    fun `test 'run' should fail when there are no acceptable commits`() {
        // GIVEN
        // WHEN
        val actual = createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME)
            .buildAndFail()

        // THEN
        assertThat(actual.output.lines())
            .contains(
                "Ignoring missing Git tag from release version calculation.",
                "Ignoring invalid commit: initialize project",
                "Ignoring invalid commit: initial commit",
                "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
            )

        assertThat(projectActions.getVersion())
            .isEqualTo(SNAPSHOT_001)
    }

    @Test
    fun `test 'run' should set release version when release is forced`() {
        // GIVEN
        // WHEN
        createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
            .build()

        // THEN
        assertThat(projectActions.getVersion())
            .isEqualTo(RELEASE_001)
    }

    @Test
    fun `test 'run' should set release version`() {
        // GIVEN
        projectDir.copyIntoFromResources("src")

        gitActions.add(projectFile, "*")
        gitActions.commit(projectFile, "feature: add request")

        // WHEN
        createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME)
            .build()

        // THEN
        assertThat(projectActions.getVersion())
            .isEqualTo(RELEASE_010)
    }

    @Test
    fun `test 'run' should set release version based on custom ConventionalCommitType`() {
        // GIVEN
        projectDir.copyIntoFromResources("build.gradle.kts.customConventionalCommitTypes")
            .renameTo(projectDir.resolve("build.gradle.kts"))

        gitActions.add(projectFile, "*")
        gitActions.commit(projectFile, "dummy: configure custom ConventionalCommitTypes")

        // WHEN
        createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME)
            .build()

        // THEN
        assertThat(projectActions.getVersion())
            .isEqualTo(RELEASE_100)
    }

}
