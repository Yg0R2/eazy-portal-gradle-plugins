package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.multimodule.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.dsl.singlemodule.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.UpdateScmTaskIntegrationTest.UpdateScmTaskMultiModuleTestCase
import org.eazyportal.plugin.gradle.release.task.UpdateScmTaskIntegrationTest.UpdateScmTaskSingleModuleTestCase
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class PrepareRepositoryForReleaseTaskIntegrationTest {

    interface PrepareRepositoryForReleaseTaskTestCase {

        fun `test 'run' from release branch should clean local commits`(): List<DynamicTest>

        fun `test 'run' from feature branch should clean local commits`(): List<DynamicTest>

//        fun `test 'run' should clean local not committed changes`(): List<DynamicTest>
//
//        fun `test 'run' should pull remote changes`(): List<DynamicTest>

    }

    interface PrepareRepositoryForReleaseTaskSingleModuleTestCase :
        PrepareRepositoryForReleaseTaskTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' from release branch should clean local commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    createAndCommitDummyFile(projectDir.localProjectFile, CHORE_COMMIT_MESSAGE)

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                            .doesNotContain(CHORE_COMMIT_MESSAGE)
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                            .doesNotContain(CHORE_COMMIT_MESSAGE)
                    }
                }
            }

        @TestFactory
        override fun `test 'run' from feature branch should clean local commits`(): List<DynamicTest>  =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    createAndCommitDummyFile(projectDir.localProjectFile, CHORE_COMMIT_MESSAGE)

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                            .doesNotContain(CHORE_COMMIT_MESSAGE)
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                            .doesNotContain(CHORE_COMMIT_MESSAGE)
                    }
                }
            }

    }

    interface PrepareRepositoryForReleaseTaskMultiModuleTestCase :
        PrepareRepositoryForReleaseTaskTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' from release branch should clean local commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        createAndCommitDummyFile(submoduleProject.localProjectFile, CHORE_COMMIT_MESSAGE)
                    }
                    scmActions.add(projectDir.localProjectFile, ".")
                    scmActions.commit(projectDir.localProjectFile, "chore: add submodule changes")

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        ).doesNotContain("chore: add submodule changes")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        scmCompareCommitsIn(submoduleProject.localProjectFile, submoduleProject.remoteProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCommitsIn(submoduleProjectDir.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                        }
                    }
                }
            }

        @TestFactory
        override fun `test 'run' from feature branch should clean local commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        createAndCommitDummyFile(submoduleProject.localProjectFile, CHORE_COMMIT_MESSAGE)
                    }
                    scmActions.add(projectDir.localProjectFile, ".")
                    scmActions.commit(projectDir.localProjectFile, "chore: add submodule changes")

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmCompareCommitsIn(projectDir.localProjectFile, projectDir.remoteProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        ).doesNotContain("chore: add submodule changes")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        scmCompareCommitsIn(submoduleProject.localProjectFile, submoduleProject.remoteProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCommitsIn(submoduleProjectDir.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                        }
                    }
                }
            }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        PrepareRepositoryForReleaseTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        PrepareRepositoryForReleaseTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class SingleModuleCustomizedTestCase :
        PrepareRepositoryForReleaseTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase()

    @Nested
    inner class MultiModuleGitFlowTestCase :
        PrepareRepositoryForReleaseTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        PrepareRepositoryForReleaseTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase()

    @Nested
    inner class MultiModuleCustomizedTestCase :
        PrepareRepositoryForReleaseTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase()

}
//
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#singleModuleTestCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' on single module project should clean local commits`(
//        testCaseClass: KClass<BaseSingleModuleScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, testBranch)
//
//            createAndCommitDummyFile(projectFile, CHORE_COMMIT_MESSAGE)
//        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//            .thenAssertTaskOutput {
//                contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
//            }.thenAssertScm {
//                it.statusCleanIn(projectFile, scmConfig.releaseBranch)
//
//                it.commitsIn(projectFile) { doesNotContain(CHORE_COMMIT_MESSAGE) }
//
//                it.compareCommitsIn(projectFile, remoteProjectFile)
//            }
//    }
//
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#multiModuleTestCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' on multi module project should clean local commits`(
//        testCaseClass: KClass<BaseMultiModuleScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(submoduleProjectFile, testBranch)
//
//            createAndCommitDummyFile(submoduleProjectFile, CHORE_COMMIT_MESSAGE)
//
//            scmActions.add(projectFile, SUBMODULE_NAME)
//            scmActions.commit(projectFile, "chore: include $SUBMODULE_NAME changes")
//        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//            .thenAssertTaskOutput {
//                contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
//            }.thenAssertScm {
//                it.statusCleanIn(projectFile, scmConfig.releaseBranch)
//                it.statusCleanIn(submoduleProjectFile, scmConfig.releaseBranch)
//
//                it.commitsIn(projectFile) { doesNotContain("chore: include $SUBMODULE_NAME changes") }
//                it.commitsIn(submoduleProjectFile) { doesNotContain(CHORE_COMMIT_MESSAGE) }
//
//                it.compareCommitsIn(projectFile, remoteProjectFile)
//                it.compareCommitsIn(submoduleProjectFile, remoteSubmoduleProjectFile)
//            }
//    }
// TODO: this
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' should clean local commits`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, testBranch)
//
//            createAndCommitDummyFile(projectFile, CHORE_COMMIT_MESSAGE)
//
//            if (this is BaseMultiModuleScmProjectTestCase) {
//                scmActions.checkout(submoduleProjectFile, testBranch)
//
//                createAndCommitDummyFile(submoduleProjectFile, CHORE_COMMIT_MESSAGE)
//
//                scmActions.add(projectFile, SUBMODULE_NAME)
//                scmActions.commit(projectFile, "chore: include $SUBMODULE_NAME changes")
//            }
//        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//            .thenAssert {
//                it.taskOutput {
//                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
//                }
//
//                it.scmLocalCommits {
//                    doesNotContain(CHORE_COMMIT_MESSAGE)
//                }
//
//                it.scmCompareCommits()
//
//                if (this is BaseMultiModuleScmProjectTestCase) {
//                    it.scmCommits(projectFile) {
//                        doesNotContain("chore: include $SUBMODULE_NAME changes")
//                    }
//
//                    it.scmCommits(submoduleProjectFile) {
//                        doesNotContain(CHORE_COMMIT_MESSAGE)
//                    }
//
//                    it.scmCompareCommits(submoduleProjectFile, remoteSubmoduleProjectFile)
//                }
//            }
//    }
// TODO: this
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' should clean local file changes`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, testBranch)
//
//            createDummyFile(projectFile)
//
//            if (this is BaseMultiModuleScmProjectTestCase) {
//                scmActions.checkout(submoduleProjectFile, testBranch)
//
//                createDummyFile(submoduleProjectFile)
//            }
//        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//            .thenAssert {
//                it.taskOutput {
//                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
//                }
//
//                it.scmStatus(projectFile) {
//                    contains(
//                        "On branch $testBranch",
//                        "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
//                        "nothing to commit, working tree clean",
//                    )
//                }
//
//                it.scmCompareCommits()
//
//                if (this is BaseMultiModuleScmProjectTestCase) {
//                    it.scmStatus(submoduleProjectFile) {
//                        contains(
//                            "On branch $testBranch",
//                            "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
//                            "nothing to commit, working tree clean",
//                        )
//                    }
//
//                    it.scmCompareCommits(submoduleProjectFile, remoteSubmoduleProjectFile)
//                }
//            }
//    }
// TODO: this
//    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
//    @ParameterizedTest
//    fun `test 'run' should pull remote changes`(
//        testCaseClass: KClass<BaseScmProjectTestCase>,
//        testBranch: String,
//        @TempDir workingDir: File,
//    ) {
//        givenTestCase(testCaseClass, workingDir) {
//            scmActions.checkout(projectFile, testBranch)
//
//            scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
//            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.releaseBranch}")
//
//            scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
//            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.featureBranch}")
//
//            if (this is BaseMultiModuleScmProjectTestCase) {
//                scmActions.checkout(submoduleProjectFile, testBranch)
//
//                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
//                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.releaseBranch}")
//
//                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
//                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.featureBranch}")
//
//                scmActions.add(remoteProjectFile, ".")
//                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
//            }
//        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//            .thenAssert {
//                it.taskOutput {
//                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
//                }
//
//                when (this) {
//                    is SingleModuleTrunkFlowScmProjectTestCase -> {
//                        it.scmCompareCommits {
//                            containsExactlyInAnyOrder(
//                                "initial commit",
//                                "chore: commit on ${scmConfig.featureBranch}",
//                                "chore: commit on ${scmConfig.releaseBranch}"
//                            ) // flaky
//                        }
//                    }
//
//                    is MultiModuleTrunkFlowScmProjectTestCase -> {
//                        it.scmCompareCommits(projectFile, remoteProjectFile) {
//                            containsExactlyInAnyOrder(
//                                "initial commit",
//                                "chore: add $SUBMODULE_NAME submodule",
//                                "chore: commit on ${scmConfig.featureBranch}",
//                                "chore: commit on ${scmConfig.releaseBranch}",
//                                "chore: include $SUBMODULE_NAME changes"
//                            ) // flaky
//                        }
//
//                        it.scmCompareCommits(submoduleProjectFile, remoteSubmoduleProjectFile) {
//                            containsExactlyInAnyOrder(
//                                "initial commit",
//                                "chore: commit on ${scmConfig.featureBranch}",
//                                "chore: commit on ${scmConfig.releaseBranch}"
//                            ) // flaky
//                        }
//                    }
//
//                    else -> {
//                        assertThat(
//                            scmActions.getCommits(projectFile, scmConfig.releaseBranch, scmConfig.featureBranch)
//                        ).contains("chore: commit on ${scmConfig.featureBranch}")
//                            .containsExactlyElementsOf(
//                                scmActions.getCommits(
//                                    remoteProjectFile,
//                                    scmConfig.releaseBranch,
//                                    scmConfig.featureBranch
//                                )
//                            )
//
//                        assertThat(
//                            scmActions.getCommits(projectFile, scmConfig.featureBranch, scmConfig.releaseBranch)
//                        ).contains("chore: commit on ${scmConfig.releaseBranch}")
//                            .containsExactlyElementsOf(
//                                scmActions.getCommits(
//                                    remoteProjectFile,
//                                    scmConfig.featureBranch,
//                                    scmConfig.releaseBranch
//                                )
//                            )
//                    }
//                }
//            }
//    }
//
//}
