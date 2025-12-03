package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class PrepareRepositoryForReleaseTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
    @ParameterizedTest
    fun `test 'run' should clean local commits`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, testBranch)

            createAndCommitDummyFile(projectFile, CHORE_COMMIT_MESSAGE)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, testBranch)

                createAndCommitDummyFile(submoduleProjectFile, CHORE_COMMIT_MESSAGE)

                scmActions.add(projectFile, SUBMODULE_NAME)
                scmActions.commit(projectFile, "chore: include $SUBMODULE_NAME changes")
            }
        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                }

                it.scmLocalCommits {
                    doesNotContain(CHORE_COMMIT_MESSAGE)
                }

                it.scmCompareCommitsAnd()

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmCommits(projectFile) {
                        doesNotContain("chore: include $SUBMODULE_NAME changes")
                    }

                    it.scmCommits(submoduleProjectFile) {
                        doesNotContain(CHORE_COMMIT_MESSAGE)
                    }

                    it.scmCompareCommitsAnd(submoduleProjectFile, remoteSubmoduleProjectFile)
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
    @ParameterizedTest
    fun `test 'run' should clean local file changes`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, testBranch)

            createDummyFile(projectFile)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, testBranch)

                createDummyFile(submoduleProjectFile)
            }
        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch $testBranch",
                        "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                        "nothing to commit, working tree clean",
                    )
                }

                it.scmCompareCommitsAnd()

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch $testBranch",
                            "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                            "nothing to commit, working tree clean",
                        )
                    }

                    it.scmCompareCommitsAnd(submoduleProjectFile, remoteSubmoduleProjectFile)
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
    @ParameterizedTest
    fun `test 'run' should pull remote changes`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, testBranch)

            scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.releaseBranch}")

            scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.featureBranch}")

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, testBranch)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.releaseBranch}")

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.featureBranch}")

                // Workaround for using none-bare repository as origin (fetch submodule changes)
                scmActions.fetch(remoteProjectFile.resolve(SUBMODULE_NAME), scmConfig.remote)

                scmActions.add(remoteProjectFile, ".")
                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
            }
        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                }

                when (this) {
                    is SingleModuleTrunkFlowScmProjectTestCase -> {
                        it.scmCompareCommitsAnd {
                            containsExactlyInAnyOrder(
                                "initial commit",
                                "chore: commit on ${scmConfig.featureBranch}",
                                "chore: commit on ${scmConfig.releaseBranch}"
                            ) // flaky
                        }
                    }

                    is MultiModuleTrunkFlowScmProjectTestCase -> {
                        it.scmCompareCommitsAnd(projectFile, remoteProjectFile) {
                            containsExactlyInAnyOrder(
                                "initial commit",
                                "chore: add $SUBMODULE_NAME submodule",
                                "chore: commit on ${scmConfig.featureBranch}",
                                "chore: commit on ${scmConfig.releaseBranch}",
                                "chore: include $SUBMODULE_NAME changes"
                            ) // flaky
                        }

                        it.scmCompareCommitsAnd(submoduleProjectFile, remoteSubmoduleProjectFile) {
                            containsExactlyInAnyOrder(
                                "initial commit",
                                "chore: commit on ${scmConfig.featureBranch}",
                                "chore: commit on ${scmConfig.releaseBranch}"
                            ) // flaky
                        }
                    }

                    else -> {
                        assertThat(
                            scmActions.getCommits(projectFile, scmConfig.releaseBranch, scmConfig.featureBranch)
                        ).contains("chore: commit on ${scmConfig.featureBranch}")
                            .containsExactlyElementsOf(
                                scmActions.getCommits(
                                    remoteProjectFile,
                                    scmConfig.releaseBranch,
                                    scmConfig.featureBranch
                                )
                            )

                        assertThat(
                            scmActions.getCommits(projectFile, scmConfig.featureBranch, scmConfig.releaseBranch)
                        ).contains("chore: commit on ${scmConfig.releaseBranch}")
                            .containsExactlyElementsOf(
                                scmActions.getCommits(
                                    remoteProjectFile,
                                    scmConfig.featureBranch,
                                    scmConfig.releaseBranch
                                )
                            )
                    }
                }
            }
    }

}
