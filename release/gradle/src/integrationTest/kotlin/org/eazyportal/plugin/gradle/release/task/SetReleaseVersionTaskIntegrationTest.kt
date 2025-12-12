package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.testcase.*
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class SetReleaseVersionTaskIntegrationTest {

    interface SetReleaseVersionTaskTestCase {

        fun `test 'run' should fail when there are no acceptable commits on`(testBranch: String)
    }

    @Nested
    inner class SetReleaseVersionTaskSingleModuleGitFlowTestCase :
        SetReleaseVersionTaskTestCase,
        SingleModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS) {

        @CsvSource(
            value = [
                ScmConstants.RELEASE_BRANCH,
                ScmConstants.FEATURE_BRANCH,
            ]
        )
        @ParameterizedTest
        override fun `test 'run' should fail when there are no acceptable commits on`(testBranch: String) = runTestCase {
            givenTestCase {
                withGradleProject()
                withScmSetUp {
                    scmActions.checkout(projectDir.localProjectFile, testBranch)
                }
            }

            whenExecute {
                taskFails(SET_RELEASE_VERSION_TASK_NAME)
            }

            thenVerify {
                taskOutput {
                    contains(
                        "Ignoring missing tag from release version calculation.",
                        "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                    )
                }

                scmClenStatusIn(projectDir.localProjectFile, testBranch)

                scmCommitsIn(projectDir.localProjectFile) {
                    containsExactly(INITIAL_COMMIT_MESSAGE)
                }

                projectVersionIn(projectDir.localProjectFile, SNAPSHOT_001)
            }
//            thenAssertScm {
//                }.thenAssertScmIfMultiModule {
//                    it.statusCleanIn(projectFile, testBranch)
//                    it.statusCleanIn(submoduleProjectFile, testBranch)
//
//                    it.commitsIn(projectFile) {
//                        containsExactly(
//                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                            INITIAL_COMMIT_MESSAGE,
//                        )
//                    }
//
//                    it.commitsIn(submoduleProjectFile) {
//                        containsExactly(INITIAL_COMMIT_MESSAGE)
//                    }
//                }.thenAssertProjectVersion(SNAPSHOT_001)
        }

    }

//    @Nested
//    inner class SetReleaseVersionTaskSingleModuleTrunkFlowTestCase :
//        SetReleaseVersionTaskTestCase,
//        SingleModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS) {
//
//
//    }
//
//    @Nested
//    inner class SetReleaseVersionTaskSingleModuleCustomizedTestCase :
//        SetReleaseVersionTaskTestCase,
//        SingleModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS) {
//
//
//    }

    @Nested
    inner class OldSetReleaseVersionTaskMultiModuleGitFlowTestCase :
        SetReleaseVersionTaskTestCase,
        OldMultiModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS) {

        override fun `test 'run' should fail when there are no acceptable commits on`(testBranch: String) {
            givenTestCase()
                .givenConfiguration {
                    scmActions.checkout(projectFile, testBranch)
                }.whenGradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
                .thenAssertTaskOutput {
                    contains(
                        "Ignoring missing tag from release version calculation.",
                        "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                    )
                }.thenAssertScm {
                    statusCleanIn(projectFile, testBranch)
//                    statusCleanIn(submoduleProjectFile, testBranch)

                    commitsIn(projectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

//                    commitsIn(submoduleProjectFile) {
//                        containsExactly(INITIAL_COMMIT_MESSAGE)
//                    }
                }.thenAssertProjectVersion(SNAPSHOT_001)
        }

    }

//    @Nested
//    inner class SetReleaseVersionTaskMultiModuleTrunkFlowTestCase :
//        SetReleaseVersionTaskTestCase,
//        MultiModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS) {
//
//
//    }
//
//    @Nested
//    inner class SetReleaseVersionTaskMultiModuleCustomizedTestCase :
//        SetReleaseVersionTaskTestCase,
//        MultiModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS) {
//
//
//    }


}

//
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' should fail when there are no acceptable commits`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, testBranch)
//        }.whenGradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
//            .thenAssertTaskOutput {
//                contains(
//                    "Ignoring missing tag from release version calculation.",
//                    "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
//                    "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
//                )
//            }.thenAssertScmIfSingleModule {
//                it.statusCleanIn(projectFile, testBranch)
//
//                it.commitsIn(projectFile) {
//                    containsExactly(INITIAL_COMMIT_MESSAGE)
//                }
//            }.thenAssertScmIfMultiModule {
//                it.statusCleanIn(projectFile, testBranch)
//                it.statusCleanIn(submoduleProjectFile, testBranch)
//
//                it.commitsIn(projectFile) {
//                    containsExactly(
//                        CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                        INITIAL_COMMIT_MESSAGE,
//                    )
//                }
//
//                it.commitsIn(submoduleProjectFile) {
//                    containsExactly(INITIAL_COMMIT_MESSAGE)
//                }
//            }.thenAssertProjectVersion(SNAPSHOT_001)
//    }
//
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
//    @ParameterizedTest
//    fun `test 'run' from release branch should succeed when there are acceptable commits on release branch`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, scmConfig.releaseBranch)
//
//            if (this is BaseSingleModuleScmProjectTestCase) {
//                createAndCommitDummyFile(projectFile, FIX_COMMIT_MESSAGE)
//            } else if (this is BaseMultiModuleScmProjectTestCase) {
//                createAndCommitDummyFile(submoduleProjectFile, FIX_COMMIT_MESSAGE)
//            }
//        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
//            .thenAssertTaskOutput {
//                contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
//            }.thenAssertScm {
//                when (this) {
//                    is BaseSingleModuleScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
//                                "Changes not staged for commit:",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                FIX_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//                    }
//
//                    is MultiModuleTrunkFlowScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
//                                "Changes not staged for commit:",
//                                "modified:   $SUBMODULE_NAME (new commits, modified content)",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//
//                    }
//
//                    is BaseMultiModuleScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
//                                "Changes not staged for commit:",
//                                "modified:   $SUBMODULE_NAME (new commits, modified content)",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//                    }
//
//                    else -> fail { "Unknown testCase type: $this" }
//                }
//            }.thenAssertProjectVersion(RELEASE_001)
//    }
//
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
//    @ParameterizedTest
//    fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, scmConfig.featureBranch)
//
//            if (this is BaseSingleModuleScmProjectTestCase) {
//                createAndCommitDummyFile(projectFile, FIX_COMMIT_MESSAGE)
//            } else if (this is BaseMultiModuleScmProjectTestCase) {
//                createAndCommitDummyFile(submoduleProjectFile, FIX_COMMIT_MESSAGE)
//            }
//        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
//            .thenAssertTaskOutput {
//                contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
//            }.thenAssertScm {
//                when (this) {
//                    is BaseSingleModuleScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
//                                "Changes not staged for commit:",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                FIX_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//                    }
//
//                    is MultiModuleTrunkFlowScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
//                                "Changes not staged for commit:",
//                                "modified:   $SUBMODULE_NAME (new commits, modified content)",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//
//                    }
//
//                    is BaseMultiModuleScmProjectTestCase -> {
//                        it.statusIn(projectFile) {
//                            contains(
//                                "On branch ${scmConfig.releaseBranch}",
//                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
//                                "Changes not staged for commit:",
//                                "modified:   $SUBMODULE_NAME (new commits, modified content)",
//                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
//                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
//                            )
//                        }
//
//                        it.commitsIn(projectFile) {
//                            containsExactly(
//                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
//                                INITIAL_COMMIT_MESSAGE,
//                            )
//                        }
//                    }
//
//                    else -> fail { "Unknown testCase type: $this" }
//                }
//            }.thenAssertProjectVersion(RELEASE_001)
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#singleModuleTestCases")
////    @ParameterizedTest
////    fun `test 'run' from feature branch on single module project should succeed when there are acceptable commits`(
////        testCaseClass: KClass<BaseSingleModuleScmProjectTestCase>,
////        @TempDir workingDir: File,
////    ) {
////        givenTestCase(testCaseClass, workingDir) {
////            scmActions.checkout(projectFile, scmConfig.featureBranch)
////
////            createAndCommitDummyFile(projectFile, FIX_COMMIT_MESSAGE)
////        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
////            .thenAssertTaskOutput {
////                contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
////            }.thenAssertScm {
////                it.statusIn(projectFile) {
////                    contains(
////                        "On branch ${scmConfig.releaseBranch}",
////                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
////                        "Changes to be committed:",
////                        "new file:   $DUMMY_FILE_NAME",
////                        "Changes not staged for commit:",
////                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                    )
////                }
////
////                it.commitsIn(projectFile) {
////                    containsExactly(INITIAL_COMMIT_MESSAGE,)
////                }
////            }.thenAssertProjectVersion(RELEASE_001)
////    }
////
////    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#multiModuleTestCasesWithBranch")
////    @ParameterizedTest
////    fun `test 'run' on multi module project should succeed when there are acceptable commits`(
////        testCaseClass: KClass<BaseMultiModuleScmProjectTestCase>,
////        testBranch: String,
////        @TempDir workingDir: File,
////    ) {
////        givenTestCase(testCaseClass, workingDir) {
////            scmActions.checkout(projectFile, testBranch)
////
////            createAndCommitDummyFile(submoduleProjectFile, FIX_COMMIT_MESSAGE)
////        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
////            .thenAssertTaskOutput {
////                contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
////            }.thenAssertScm {
////                it.statusCleanIn(projectFile, testBranch)
////
////                it.commitsIn(projectFile) {
////                    containsExactly(
////                        ADD_SUBMODULES_COMMIT_MESSAGE,
////                        INITIAL_COMMIT_MESSAGE,
////                    )
////                }
////
////                it.commitsIn(submoduleProjectFile) {
////                    containsExactly(
////                        FIX_COMMIT_MESSAGE,
////                        INITIAL_COMMIT_MESSAGE,
////                    )
////                }
////
////                it.statusCleanIn(submoduleProjectFile, testBranch)
////            }.thenAssertProjectVersion(SNAPSHOT_001)
////    }
////
////    // TODO: test 'run' should fail from release branch when feature branch has acceptable commits
////    // TODO: test 'run' should succeed from release branch when release branch has acceptable commits
////    // TODO: test 'run' should succeed from feature branch when feature branch has acceptable commits
////    // TODO: test 'run' should succeed from feature branch when release branch has acceptable commits
////
////    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
////    @ParameterizedTest
////    fun `test 'run' should set release version`(
////        testCaseClass: KClass<BaseScmProjectTestCase>,
////        @TempDir workingDir: File,
////    ) {
////        givenTestCase(testCaseClass, workingDir) {
////            scmActions.checkout(projectFile, scmConfig.featureBranch)
////
////            if ((this is SingleModuleCustomizedProjectTestCase) || (this is MultiModuleCustomizedProjectTestCase)) {
////                createAndCommitDummyFile(projectFile, "dummy: configure custom ConventionalCommitTypes")
////            } else {
////                createAndCommitDummyFile(projectFile, FEATURE_COMMIT_MESSAGE)
////            }
////        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
////            .thenAssert {
////                it.taskOutput {
////                    contains(
////                        "Ignoring missing Git tag from release version calculation.",
////                        "Ignoring invalid commit: initial commit",
////                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
////                    )
////                }
////
////                if ((this is SingleModuleTrunkFlowScmProjectTestCase) || (this is MultiModuleTrunkFlowScmProjectTestCase)) {
////                    it.scmStatus(projectFile) {
////                        contains(
////                            "On branch ${scmConfig.releaseBranch}",
////                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
////                            "Changes not staged for commit:",
////                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
////                        )
////                    }
////                } else {
////                    it.scmStatus(projectFile) {
////                        contains(
////                            "On branch ${scmConfig.releaseBranch}",
////                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
////                            "Changes to be committed:",
////                            "new file:   $DUMMY_FILE_NAME",
////                            "Changes not staged for commit:",
////                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                        )
////                    }
////                }
////
////                if (this is BaseMultiModuleScmProjectTestCase) {
////                    it.scmStatus(submoduleProjectFile) {
////                        contains(
////                            "On branch ${scmConfig.releaseBranch}",
////                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
////                            "Changes not staged for commit:",
////                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
////                        )
////                    }
////                }
////
////                if ((this is SingleModuleCustomizedProjectTestCase) || (this is MultiModuleCustomizedProjectTestCase)) {
////                    it.projectVersion {
////                        isEqualTo(RELEASE_100)
////                    }
////                } else {
////                    it.projectVersion {
////                        isEqualTo(RELEASE_010)
////                    }
////                }
////            }
////    }
////
////    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
////    @ParameterizedTest
////    fun `test 'run' should set release version when release is forced`(
////        testCaseClass: KClass<BaseScmProjectTestCase>,
////        @TempDir workingDir: File,
////    ) {
////        givenTestCase(testCaseClass, workingDir) {
////            scmActions.checkout(projectFile, scmConfig.featureBranch)
////        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
////            .thenAssert {
////                it.taskOutput {
////                    contains(
////                        "Ignoring missing Git tag from release version calculation.",
////                        "Ignoring invalid commit: initial commit",
////                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
////                    )
////                }
////
////                it.scmStatus(projectFile) {
////                    contains(
////                        "On branch ${scmConfig.releaseBranch}",
////                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
////                        "Changes not staged for commit:",
////                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
////                    )
////                }
////
////                if (this is BaseMultiModuleScmProjectTestCase) {
////                    it.scmStatus(submoduleProjectFile) {
////                        contains(
////                            "On branch ${scmConfig.releaseBranch}",
////                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
////                            "Changes not staged for commit:",
////                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
////                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
////                        )
////                    }
////                }
////
////                it.projectVersion {
////                    isEqualTo(RELEASE_001)
////                }
////            }
////    }
//
//}
