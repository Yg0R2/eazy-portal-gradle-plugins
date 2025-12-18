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

        fun `test 'run' from release branch should clean local not committed changes`(): List<DynamicTest>

        fun `test 'run' from feature branch should clean local not committed changes`(): List<DynamicTest>

//        fun `test 'run' from release branch should pull remote changes from release branch`()
//        fun `test 'run' from feature branch should pull remote changes from feature branch`()
        fun `test 'run' should pull remote changes`(): List<DynamicTest>


//        fun `test 'run' from feature branch should pull remote changes from release branch`()
//        fun `test 'run' from release branch should not pull remote changes from feature branch`()
//


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

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

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

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                            .doesNotContain(CHORE_COMMIT_MESSAGE)
                    }
                }
            }

        @TestFactory // TODO: fix implementation - does not clean repository changes during preparation
        override fun `test 'run' from release branch should clean local not committed changes`(): List<DynamicTest>  =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    createDummyFile(projectDir.localProjectFile)

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)
                }
            }

        @TestFactory // TODO: fix implementation - does not clean repository changes during preparation
        override fun `test 'run' from feature branch should clean local not committed changes`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    createDummyFile(projectDir.localProjectFile)

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)
                }
            }

        @TestFactory
        override fun `test 'run' should pull remote changes`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { testBranch -> "when running from $testBranch branch and there are commits on $testBranch branch" }
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.remoteProjectFile, testBranch)

                    createAndCommitDummyFile(projectDir.remoteProjectFile, CHORE_COMMIT_MESSAGE)

                    scmActions.checkout(projectDir.localProjectFile, testBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, testBranch)

                    scmCompareCommitsIn(projectDir.remoteProjectFile, projectDir.localProjectFile) {
                        containsExactly(
                            CHORE_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
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

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCompareCommitsIn(submoduleProjectDir.localProjectFile, submoduleProjectDir.remoteProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }

                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.releaseBranch)
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCommitsIn(submoduleProjectDir.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }

                        scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)
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

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCompareCommitsIn(submoduleProjectDir.localProjectFile, submoduleProjectDir.remoteProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }

                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.featureBranch)
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmCommitsIn(submoduleProjectDir.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                                .doesNotContain(CHORE_COMMIT_MESSAGE)
                        }

                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.releaseBranch)
                    }
                }
            }

        @TestFactory // TODO: fix implementation - does not clean repository changes during preparation
        override fun `test 'run' from release branch should clean local not committed changes`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        createDummyFile(submoduleProject.localProjectFile)
                    }

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.releaseBranch)
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.featureBranch)
                    }
                }
            }

        @TestFactory // TODO: fix implementation - does not clean repository changes during preparation
        override fun `test 'run' from feature branch should clean local not committed changes`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "on $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject()

                    scmActions.checkout(projectDir.localProjectFile, testBranch)

                    submoduleProjectDirs.forEach { submoduleProject ->
                        createDummyFile(submoduleProject.localProjectFile)
                    }

                    scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                }

                whenExecute {
                    gradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                    }

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.featureBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.featureBranch)
                    }

                    // Validate feature branch commits
                    scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                    scmClenStatusIn(projectDir.localProjectFile, scmConfig.releaseBranch)

                    submoduleProjectDirs.forEach { submoduleProjectDir ->
                        scmClenStatusIn(submoduleProjectDir.localProjectFile, scmConfig.releaseBranch)
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should pull remote changes`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(
                    scmConfig.releaseBranch to scmConfig.releaseBranch,
                    scmConfig.featureBranch to scmConfig.featureBranch,
                    scmConfig.featureBranch to scmConfig.releaseBranch,
                ),
                { (testBranch, commitsOnBranch) -> "when running from $testBranch branch and there are commits on $commitsOnBranch branch" }
            ) { (testBranch, commitsOnBranch) ->

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
