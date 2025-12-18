package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class FinalizeReleaseVersionTaskIntegrationTest {

    interface FinalizeReleaseVersionTaskTestCase {

        fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest>

        fun `test 'run' should finalize release version`(): List<DynamicTest>

    }

    interface FinalizeReleaseVersionTaskSingleModuleTestCase :
        FinalizeReleaseVersionTaskTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)
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

        @TestFactory
        override fun `test 'run' should finalize release version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    setProjectVersion(projectDir.localProjectFile, RELEASE_001)
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

                        projectVersionIn(this) {
                            isEqualTo(RELEASE_001)
                        }
                    }
                }
            }

    }

    interface FinalizeReleaseVersionTaskMultiModuleTestCase :
        FinalizeReleaseVersionTaskTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)
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

        @TestFactory
        override fun `test 'run' should finalize release version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    allProjectDirs.forEach {
                        setProjectVersion(it.localProjectFile, RELEASE_001)
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

                        projectVersionIn(this) {
                            isEqualTo(RELEASE_001)
                        }
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

                            projectVersionIn(this) {
                                isEqualTo(RELEASE_001)
                            }
                        }
                    }
                }
            }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleCustomizedTestCase :
        FinalizeReleaseVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleCustomizedTestCase :
        FinalizeReleaseVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()

}
