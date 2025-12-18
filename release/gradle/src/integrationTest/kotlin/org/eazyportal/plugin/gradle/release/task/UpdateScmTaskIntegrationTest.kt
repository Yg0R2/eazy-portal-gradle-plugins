package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class UpdateScmTaskIntegrationTest {

    interface UpdateScmTaskTestCase {

        fun `test 'run' should update SCM with commits`(): List<DynamicTest>

    }

    interface UpdateScmTaskSingleModuleTestCase :
        UpdateScmTaskTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should update SCM with commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    createAndCommitDummyFile(projectDir.localProjectFile, CHORE_COMMIT_MESSAGE)
                }

                whenExecute {
                    gradleTaskSucceeds(UPDATE_SCM_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$UPDATE_SCM_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, testBranch)

                    // Workaround for using none-bare repository as origin
                    with(projectDir.remoteProjectFile) {
                        scmActions.clean(this)
                        scmActions.checkout(this, testBranch)
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(
                            CHORE_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }
                }
            }

    }

    interface UpdateScmTaskMultiModuleTestCase :
        UpdateScmTaskTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should update SCM with commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        createAndCommitDummyFile(submoduleProject.localProjectFile, CHORE_COMMIT_MESSAGE)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(UPDATE_SCM_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$UPDATE_SCM_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch $testBranch",
                            "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                            "Changes not staged for commit:",
                            *SUBMODULE_NAMES.map { "modified:   $it (new commits)" }
                                .toTypedArray(),
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    submoduleProjectDirs.forEach {
                        scmClenStatusIn(it.localProjectFile, testBranch)
                    }

                    // Workaround for using none-bare repository as origin
                    with(projectDir.remoteProjectFile) {
                        scmActions.clean(this)
                        scmActions.checkout(this, testBranch)
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE
                        )
                    }

                    submoduleProjectDirs.forEach {
                        scmCompareCommitsIn(it.localProjectFile, it.remoteProjectFile) {
                            containsExactly(
                                CHORE_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }
                    }
                }
            }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        UpdateScmTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        UpdateScmTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleCustomizedTestCase :
        UpdateScmTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        UpdateScmTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        UpdateScmTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleCustomizedTestCase :
        UpdateScmTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()

}
