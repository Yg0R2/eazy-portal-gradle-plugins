package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class SetSnapshotVersionTaskIntegrationTest {

    interface SetSnapshotVersionTaskTestCase {

        fun `test 'run' should set SNASPSHOT version`(): List<DynamicTest>

//        fun `test 'run' should fail when project is already on SNASPSHOT version`(): List<DynamicTest>

    }

    interface SetSnapshotVersionTaskSingleModuleTestCase :
        SetSnapshotVersionTaskTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should set SNASPSHOT version`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" }
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    setProjectVersion(projectDir.localProjectFile, RELEASE_001)
                    scmActions.add(projectDir.localProjectFile, ".")
                    scmActions.commit(projectDir.localProjectFile, "Release version: $RELEASE_001")
                    scmActions.push(projectDir.localProjectFile, scmConfig.remote, testBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    projectVersionIn(projectDir.localProjectFile) {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
            }

    }

    interface SetSnapshotVersionTaskMultiModuleTestCase :
        SetSnapshotVersionTaskTestCase,
        MultiModuleScmProjectTestCase {

        override fun `test 'run' should set SNASPSHOT version`(): List<DynamicTest> {
            TODO("Not yet implemented")
        }

    }


    @Nested
    inner class SingleModuleGitFlowTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleCustomizedTestCase :
        SetSnapshotVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleCustomizedTestCase :
        SetSnapshotVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()


    /*
        @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
        @ParameterizedTest
        fun `test 'run' should fail when project version is not release version`(
            testCaseClass: KClass<BaseScmProjectTestCase>,
            @TempDir workingDir: File,
        ) {
            givenTestCase(testCaseClass, workingDir) {
                scmActions.checkout(projectFile, scmConfig.featureBranch)

                if (this is BaseMultiModuleScmProjectTestCase) {
                    scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
                }

                setProjectVersion(SNAPSHOT_001)
            }.whenGradleTaskFails(SET_SNAPSHOT_VERSION_TASK_NAME)
                .thenAssert {
                    it.taskOutput {
                        contains(
                            "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                            "> Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version.",
                        )
                    }

                    it.scmStatus(projectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "nothing to commit, working tree clean"
                        )
                    }

                    if (this is BaseMultiModuleScmProjectTestCase) {
                        it.scmStatus(submoduleProjectFile) {
                            contains(
                                "On branch ${scmConfig.featureBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                                "nothing to commit, working tree clean"
                            )
                        }
                    }

                    it.projectVersion {
                        isEqualTo(SNAPSHOT_001)
                    }
                }
        }

        @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
        @ParameterizedTest
        fun `test 'run' should set snapshot version`(
            testCaseClass: KClass<BaseScmProjectTestCase>,
            @TempDir workingDir: File,
        ) {
            givenTestCase(testCaseClass, workingDir) {
                scmActions.checkout(projectFile, scmConfig.featureBranch)

                if (this is BaseMultiModuleScmProjectTestCase) {
                    scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
                }

                setProjectVersion(RELEASE_001)
            }.whenGradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
                .thenAssert {
                    it.taskOutput {
                        contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                    }

                    it.scmStatus(projectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    if (this is BaseMultiModuleScmProjectTestCase) {
                        it.scmStatus(submoduleProjectFile) {
                            contains(
                                "On branch ${scmConfig.featureBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }
                    }

                    it.projectVersion {
                        isEqualTo(SNAPSHOT_002)
                    }
                }
        }
    */
}
