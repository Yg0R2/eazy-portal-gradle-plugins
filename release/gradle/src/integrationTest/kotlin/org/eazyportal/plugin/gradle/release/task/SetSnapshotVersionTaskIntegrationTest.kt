package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.ScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.project.GradleProjectConstants.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SetSnapshotVersionTaskIntegrationTest {

    @Nested
    inner class SingleModuleGitProject :
        SingleModuleScmProjectBaseIntegrationTest(GitUtils),
        BaseSetSnapshotVersionTaskIntegrationTest

    private interface BaseSetSnapshotVersionTaskIntegrationTest : ScmProjectIntegrationTest {

        @BeforeEach
        fun setUp() {
            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
        }

        @Test
        fun `test 'run' should fail when project version is not release version`() {
            // GIVEN
            scmUtils.createDummyFile(projectDir)

            // WHEN
            val actual = createGradleRunner(projectDir, SET_SNAPSHOT_VERSION_TASK_NAME)
                .buildAndFail()

            // THEN
            assertThat(actual.output.lines())
                .contains(
                    "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                    "> Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version.",
                )

            assertThat(getProjectVersion(projectDir))
                .isEqualTo(SNAPSHOT_001)
        }

        @Test
        fun `test 'run' should set snapshot version`() {
            // GIVEN
            projectDir.copyIntoFromResources(
                SetSnapshotVersionTaskIntegrationTest::class.java.simpleName,
                "$GRADLE_PROPERTIES_FILE_NAME.release-version"
            ).renameTo(projectDir.resolve(GRADLE_PROPERTIES_FILE_NAME))

            // WHEN
            createGradleRunner(projectDir, SET_SNAPSHOT_VERSION_TASK_NAME)
                .build()

            // THEN
            assertThat(getProjectVersion(projectDir))
                .isEqualTo(SNAPSHOT_002)
        }

    }

}
