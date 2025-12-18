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
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class FinalizeSnapshotVersionTaskIntegrationTest {

    interface FinalizeSnapshotVersionTestCase {

        fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest>

        fun `test 'run' should finalize snapshot version`(): List<DynamicTest>

    }

    interface FinalizeSnapshotVersionTaskSingleModuleTestCase :
        FinalizeSnapshotVersionTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)
                }

                whenExecute {
                    gradleTaskFails(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME FAILED",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should finalize snapshot version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    setProjectVersion(projectDir.localProjectFile, SNAPSHOT_002)
                }

                whenExecute {
                    gradleTaskSucceeds(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME")
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
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this) {
                            isEqualTo(SNAPSHOT_002)
                        }
                    }
                }
            }

    }

    interface FinalizeSnapshotVersionTaskMultiModuleTestCase :
        FinalizeSnapshotVersionTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there is nothing to commit`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)
                }

                whenExecute {
                    gradleTaskFails(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME FAILED",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
            }

        override fun `test 'run' should finalize snapshot version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    setProjectVersion(projectDir.localProjectFile, SNAPSHOT_002)
                }

                whenExecute {
                    gradleTaskSucceeds(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME")
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
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this) {
                            isEqualTo(SNAPSHOT_002)
                        }
                    }

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmStatusIn(submoduleProjectDir.localProjectFile) {
                            contains(
                                "On branch $testBranch",
                                "Your branch is ahead of '${scmConfig.remote}/$testBranch' by 1 commit.",
                                "nothing to commit, working tree clean"
                            )
                        }

                        scmCommitsIn(submoduleProjectDir.localProjectFile) {
                            containsExactly(
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(submoduleProjectDir.localProjectFile) {
                            isEqualTo(SNAPSHOT_002)
                        }
                    }
                }
            }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        FinalizeSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        FinalizeSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleCustomizedTestCase :
        FinalizeSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        FinalizeSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        FinalizeSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleCustomizedTestCase :
        FinalizeSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()

}
