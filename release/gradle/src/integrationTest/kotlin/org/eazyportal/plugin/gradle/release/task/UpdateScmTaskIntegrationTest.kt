package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.ScmProjectIntegrationTest
import org.eazyportal.plugin.gradle.SingleModuleScmProjectBaseIntegrationTest
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UpdateScmTaskIntegrationTest {

    @Nested
    inner class SingleModuleGitProject :
        SingleModuleScmProjectBaseIntegrationTest(GitUtils),
        BaseUpdateScmTaskIntegrationTest

    private interface BaseUpdateScmTaskIntegrationTest : ScmProjectIntegrationTest {

        @BeforeEach
        fun setUp() {
            // Initialize both branch locally
            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should update SCM with commits`(testBranch: String) {
            // GIVEN
            scmUtils.checkout(projectDir, testBranch)

            scmUtils.createDummyCommit(projectDir, testBranch)

            // WHEN
            val actual = createGradleRunner(projectDir, UPDATE_SCM_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$UPDATE_SCM_TASK_NAME")

            // Workaround for using none-bare repository
            scmUtils.clean(remoteProjectDir)
            scmUtils.checkout(remoteProjectDir, testBranch)

            assertThat(scmUtils.getCommits(projectDir))
                .containsExactlyElementsOf(scmUtils.getCommits(remoteProjectDir))
        }

    }

}
