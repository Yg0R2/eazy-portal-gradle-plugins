package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.testcase.*
import org.eazyportal.plugin.gradle.release.testcase.dsl.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.OldSingleModuleScmProjectTestCase
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class FinalizeReleaseVersionTaskIntegrationTest {

    interface FinalizeReleaseVersionTaskTestCase {

        fun `test 'run' should finalize release version`(): List<DynamicTest>

        fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest>

    }

    interface FinalizeReleaseVersionTaskSingleModuleTestCase :
        FinalizeReleaseVersionTaskTestCase,
        OldSingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should finalize release version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)

                        setProjectVersion(RELEASE_001)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(FINALIZE_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch $testBranch",
                                "Your branch is ahead of '${scmConfig.remote}/$testBranch' by 1 commit.",
                                "nothing to commit, working tree clean"
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                "Release version: $RELEASE_001",
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(FINALIZE_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME FAILED",
                            "nothing to commit, working tree clean",
                            "Execution failed for task ':$FINALIZE_RELEASE_VERSION_TASK_NAME'."
                        )
                    }
                }
            }

    }

    interface FinalizeReleaseVersionTaskMultiModuleTestCase :
        FinalizeReleaseVersionTaskTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should finalize release version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)

                        setProjectVersion(RELEASE_001)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(FINALIZE_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch $testBranch",
                                "Your branch is ahead of '${scmConfig.remote}/$testBranch' by 1 commit.",
                                "nothing to commit, working tree clean"
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                "Release version: $RELEASE_001",
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        with(it.localProjectFile) {
                            scmStatusIn(this) {
                                contains(
                                    "On branch $testBranch",
                                    "Your branch is ahead of '${scmConfig.remote}/$testBranch' by 1 commit.",
                                    "nothing to commit, working tree clean"
                                )
                            }

                            scmCommitsIn(this) {
                                containsExactly(
                                    "Release version: $RELEASE_001",
                                    INITIAL_COMMIT_MESSAGE,
                                )
                            }

                            projectVersionIn(this, RELEASE_001)
                        }
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(FINALIZE_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME FAILED",
                            "nothing to commit, working tree clean",
                            "Execution failed for task ':$FINALIZE_RELEASE_VERSION_TASK_NAME'."
                        )
                    }
                }
            }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        OldSingleModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class SingleModuleCustomizedTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class MultiModuleGitFlowTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class MultiModuleCustomizedTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS)

}
