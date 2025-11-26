package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.ScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_010
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_100
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetReleaseVersionTaskIntegrationTest {

    @Nested
    inner class SingleModuleGitProject :
        SingleModuleScmProjectBaseIntegrationTest(GitUtils),
        BaseSetReleaseVersionTaskIntegrationTest {

        override fun setupRemoteBeforeClone() {
            super.setupRemoteBeforeClone()

            // Create dev branch in origin
            remoteProjectDir.git("branch", ScmConstants.FEATURE_BRANCH)
        }
    }

    private interface BaseSetReleaseVersionTaskIntegrationTest : ScmProjectIntegrationTest {

        @BeforeEach
        fun setUp() {
            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
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
                    "Ignoring invalid commit: initial commit",
                    "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                )

            assertThat(getProjectVersion(projectDir))
                .isEqualTo(SNAPSHOT_001)
        }

        @Test
        fun `test 'run' should set release version when release is forced`() {
            // GIVEN
            // WHEN
            createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
                .build()

            // THEN
            assertThat(getProjectVersion(projectDir))
                .isEqualTo(RELEASE_001)
        }

        @Test
        fun `test 'run' should set release version`() {
            // GIVEN
            projectDir.copyIntoFromResources(SetReleaseVersionTaskIntegrationTest::class.java.simpleName, "src")

            scmUtils.add(projectDir, ".")
            scmUtils.commit(projectDir, "feature: add request")

            // WHEN
            createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME)
                .build()

            // THEN
            assertThat(getProjectVersion(projectDir))
                .isEqualTo(RELEASE_010)
        }

        @Test
        fun `test 'run' should set release version based on custom ConventionalCommitType`() {
            // GIVEN
            projectDir.copyIntoFromResources(
                SetReleaseVersionTaskIntegrationTest::class.java.simpleName,
                "build.gradle.kts.customConventionalCommitTypes"
            ).renameTo(projectDir.resolve("build.gradle.kts"))

            scmUtils.add(projectDir, ".")
            scmUtils.commit(projectDir, "dummy: configure custom ConventionalCommitTypes")

            // WHEN
            createGradleRunner(projectDir, SET_RELEASE_VERSION_TASK_NAME)
                .build()

            // THEN
            assertThat(getProjectVersion(projectDir))
                .isEqualTo(RELEASE_100)
        }

    }

}
