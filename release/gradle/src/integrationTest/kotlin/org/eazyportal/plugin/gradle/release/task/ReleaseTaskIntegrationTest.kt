package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.FEATURE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.FIX_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_TAG
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class ReleaseTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
    @ParameterizedTest
    fun `test 'release' should fail when there are no acceptable commits`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)
            }

            scmActions.checkout(projectFile, testBranch)
        }.whenGradleTaskFails(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                        "> There are no acceptable commits.",
                    )
                }

                assertFailedRelease(
                    projectFile = projectFile,
                    remoteProjectFile = remoteProjectFile,
                )

                if (this is BaseMultiModuleScmProjectTestCase) {
                    assertFailedRelease(
                        projectFile = submoduleProjectFile,
                        remoteProjectFile = remoteSubmoduleProjectFile,
                    )
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should fail (OR succeed) from release branch when there are acceptable commits on feature branch`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
            createAndCommitDummyFile(remoteProjectFile, FIX_COMMIT_MESSAGE)

            if (this@givenTestCase is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)

                scmActions.add(remoteProjectFile, ".")
                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
            }

            scmActions.checkout(projectFile, scmConfig.releaseBranch)
        }.whenGradleTask(RELEASE_TASK_NAME) {
            if ((this is SingleModuleTrunkFlowScmProjectTestCase) || (this is MultiModuleTrunkFlowScmProjectTestCase)) {
                it.build()
            } else {
                it.buildAndFail()
            }
        }.thenAssert {
            if ((this is SingleModuleTrunkFlowScmProjectTestCase) || (this is MultiModuleTrunkFlowScmProjectTestCase)) {
                it.taskOutput {
                    contains(
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                        "> Task :build",
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$UPDATE_SCM_TASK_NAME",
                        "> Task :$RELEASE_TASK_NAME",
                    )
                }

                if (this is SingleModuleTrunkFlowScmProjectTestCase) {
                    listOf(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                    ).run {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                            releaseBranchCommits = this,
                            featureBranchCommits = this,
                        )
                    }
                } else if (this is MultiModuleTrunkFlowScmProjectTestCase) {
                    listOf(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                    ).run {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                            releaseBranchCommits = this,
                            featureBranchCommits = this,
                        )
                    }

                    listOf(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                    ).run {
                        assertSucceededRelease(
                            projectFile = submoduleProjectFile,
                            remoteProjectFile = remoteSubmoduleProjectFile,
                            releaseBranchCommits = this,
                            featureBranchCommits = this,
                        )
                    }
                }
            } else {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                        "> There are no acceptable commits.",
                    )
                }

                if (this is BaseSingleModuleScmProjectTestCase) {
                    assertFailedRelease(
                        projectFile = projectFile,
                        remoteProjectFile = remoteProjectFile,
                        featureBranchCommits = listOf(FIX_COMMIT_MESSAGE)
                    )
                } else if (this is BaseMultiModuleScmProjectTestCase) {
                    assertFailedRelease(
                        projectFile = projectFile,
                        remoteProjectFile = remoteProjectFile,
                        featureBranchCommits = listOf(
                            "chore: include $SUBMODULE_NAME changes",
                            FIX_COMMIT_MESSAGE,
                        )
                    )

                    assertFailedRelease(
                        projectFile = submoduleProjectFile,
                        remoteProjectFile = remoteSubmoduleProjectFile,
                        featureBranchCommits = listOf(FIX_COMMIT_MESSAGE)
                    )
                }
            }
        }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should succeed from release branch when there are acceptable commits on release branch`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
            createAndCommitDummyFile(remoteProjectFile, FIX_COMMIT_MESSAGE)

            if (this@givenTestCase is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)

                scmActions.add(remoteProjectFile, ".")
                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
            }

            scmActions.checkout(projectFile, scmConfig.releaseBranch)
        }.whenGradleTaskSucceeds(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                        "> Task :build",
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$UPDATE_SCM_TASK_NAME",
                        "> Task :$RELEASE_TASK_NAME",
                    )
                }

                when (this) {
                    is SingleModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is MultiModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            "chore: include $SUBMODULE_NAME changes",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }

                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = submoduleProjectFile,
                                remoteProjectFile = remoteSubmoduleProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is BaseSingleModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                        )
                    }

                    is BaseMultiModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                            releaseBranchCommits = listOf(
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                            featureBranchCommits = listOf(
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                        )

                        assertSucceededRelease(
                            projectFile = submoduleProjectFile,
                            remoteProjectFile = remoteSubmoduleProjectFile,
                        )
                    }
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should succeed from feature branch when there are acceptable commits on feature branch`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
            createAndCommitDummyFile(remoteProjectFile, FIX_COMMIT_MESSAGE)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)

                scmActions.add(remoteProjectFile, ".")
                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
            }

            scmActions.checkout(projectFile, scmConfig.featureBranch)
        }.whenGradleTaskSucceeds(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                        "> Task :build",
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$UPDATE_SCM_TASK_NAME",
                        "> Task :$RELEASE_TASK_NAME",
                    )
                }

                when (this) {
                    is SingleModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is MultiModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            "chore: include $SUBMODULE_NAME changes",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }

                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = submoduleProjectFile,
                                remoteProjectFile = remoteSubmoduleProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is BaseSingleModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                        )
                    }

                    is BaseMultiModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                            releaseBranchCommits = listOf(
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                            featureBranchCommits = listOf(
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                        )

                        assertSucceededRelease(
                            projectFile = submoduleProjectFile,
                            remoteProjectFile = remoteSubmoduleProjectFile,
                        )
                    }
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should succeed from feature branch when there are acceptable commits on release branch`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
            createAndCommitDummyFile(remoteProjectFile, FIX_COMMIT_MESSAGE)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)

                scmActions.add(remoteProjectFile, ".")
                scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
            }

            scmActions.checkout(projectFile, scmConfig.featureBranch)
        }.whenGradleTaskSucceeds(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
                        "> Task :build",
                        "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
                        "> Task :$UPDATE_SCM_TASK_NAME",
                        "> Task :$RELEASE_TASK_NAME",
                    )
                }

                when (this) {
                    is SingleModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is MultiModuleTrunkFlowScmProjectTestCase -> {
                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            "chore: include $SUBMODULE_NAME changes",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = projectFile,
                                remoteProjectFile = remoteProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }

                        listOf(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "Release version: $RELEASE_001",
                            FIX_COMMIT_MESSAGE,
                        ).run {
                            assertSucceededRelease(
                                projectFile = submoduleProjectFile,
                                remoteProjectFile = remoteSubmoduleProjectFile,
                                releaseBranchCommits = this,
                                featureBranchCommits = this,
                            )
                        }
                    }

                    is BaseSingleModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                        )
                    }

                    is BaseMultiModuleScmProjectTestCase -> {
                        assertSucceededRelease(
                            projectFile = projectFile,
                            remoteProjectFile = remoteProjectFile,
                            releaseBranchCommits = listOf(
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                            featureBranchCommits = listOf(
                                "New SNAPSHOT version: $SNAPSHOT_002",
                                "Release version: $RELEASE_001",
                                "chore: include $SUBMODULE_NAME changes",
                                FIX_COMMIT_MESSAGE,
                            ),
                        )

                        assertSucceededRelease(
                            projectFile = submoduleProjectFile,
                            remoteProjectFile = remoteSubmoduleProjectFile,
                        )
                    }
                }
            }
    }






    /*@MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should fail from release branch when there are acceptable commits on submodule release branch but not committed to project`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            if (this@givenTestCase is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)
            }

            scmActions.checkout(projectFile, scmConfig.releaseBranch)
        }.whenGradleTaskFails(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                        "> There are no acceptable commits.",
                    )
                }

//                assertFailedRelease(
//                    featureBranchCommits = listOf(FIX_COMMIT_MESSAGE)
//                )
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'release' should succeed from release branch when there are acceptable commits on submodule release branch and committed to project`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            val initialVersion = Version.of(INITIAL_TAG)

            scmActions.tag(remoteProjectFile, initialVersion)

            if (this@givenTestCase is BaseMultiModuleScmProjectTestCase) {
                scmActions.tag(remoteSubmoduleProjectFile, initialVersion)

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)
            }

            scmActions.checkout(projectFile, scmConfig.releaseBranch)
        }.whenGradleTaskSucceeds(RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                        "> There are no acceptable commits.",
                    )
                }

//                assertFailedRelease(
//                    featureBranchCommits = listOf(FIX_COMMIT_MESSAGE)
//                )
            }
    }*/


    private fun BaseScmProjectTestCase.assertFailedRelease(
        projectFile: ProjectFile<File>,
        remoteProjectFile: ProjectFile<File>,
        releaseBranchCommits: List<String> = emptyList(),
        featureBranchCommits: List<String> = emptyList(),
    ) {
        scmActions.clean(projectFile)
        scmActions.clean(remoteProjectFile)

        // assert project version
        listOf(scmConfig.releaseBranch, scmConfig.releaseBranch).forEach { branch ->
            scmActions.checkout(projectFile, branch)
            assertThat(getProjectVersion(projectFile))
                .isEqualTo(SNAPSHOT_001)

            scmActions.checkout(remoteProjectFile, branch)
            assertThat(getProjectVersion(remoteProjectFile))
                .isEqualTo(SNAPSHOT_001)
        }

        assertRepositories(projectFile, remoteProjectFile, releaseBranchCommits, featureBranchCommits)
    }

    private fun BaseScmProjectTestCase.assertSucceededRelease(
        projectFile: ProjectFile<File>,
        remoteProjectFile: ProjectFile<File>,
        releaseBranchCommits: List<String> = listOf(
            "Release version: $RELEASE_001",
            FIX_COMMIT_MESSAGE,
        ),
        featureBranchCommits: List<String> = listOf(
            "New SNAPSHOT version: $SNAPSHOT_002",
            "Release version: $RELEASE_001",
            FIX_COMMIT_MESSAGE,
        ),
    ) {
        scmActions.clean(projectFile)
        scmActions.clean(remoteProjectFile)

        // assert project version
        scmActions.checkout(projectFile, scmConfig.featureBranch)
        assertThat(getProjectVersion(projectFile))
            .isEqualTo(SNAPSHOT_002)
        scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
        assertThat(getProjectVersion(remoteProjectFile))
            .isEqualTo(SNAPSHOT_002)

        val newVersion =
            if ((this is SingleModuleTrunkFlowScmProjectTestCase) || (this is MultiModuleTrunkFlowScmProjectTestCase)) {
                SNAPSHOT_002
            } else {
                RELEASE_001
            }
        scmActions.checkout(projectFile, scmConfig.releaseBranch)
        assertThat(getProjectVersion(projectFile))
            .isEqualTo(newVersion)
        scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
        assertThat(getProjectVersion(remoteProjectFile))
            .isEqualTo(newVersion)

        assertRepositories(
            projectFile,
            remoteProjectFile,
            releaseBranchCommits,
            featureBranchCommits,
            RELEASE_001.toString(),
        )
    }

    fun BaseScmProjectTestCase.assertRepositories(
        projectFile: ProjectFile<File>,
        remoteProjectFile: ProjectFile<File>,
        releaseBranchCommits: List<String>,
        featureBranchCommits: List<String>,
        lastTag: String = INITIAL_TAG,
    ) {
        // assert commits
        assertThat(scmActions.getCommits(projectFile, INITIAL_TAG, "${scmConfig.remote}/${scmConfig.releaseBranch}"))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmActions.getCommits(projectFile, INITIAL_TAG, "${scmConfig.remote}/${scmConfig.featureBranch}"))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
        assertThat(scmActions.getCommits(projectFile, INITIAL_TAG, scmConfig.releaseBranch))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmActions.getCommits(projectFile, INITIAL_TAG, scmConfig.featureBranch))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        assertThat(scmActions.getCommits(remoteProjectFile, INITIAL_TAG, scmConfig.releaseBranch))
            .containsExactlyElementsOf(releaseBranchCommits)
        assertThat(scmActions.getCommits(remoteProjectFile, INITIAL_TAG, scmConfig.featureBranch))
            .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky

        // assert tags
        assertThat(scmActions.getLastTag(projectFile, scmConfig.releaseBranch))
            .isEqualTo(lastTag)

        assertThat(scmActions.getLastTag(remoteProjectFile, scmConfig.releaseBranch))
            .isEqualTo(lastTag)

        val expectedTags = setOf(lastTag, INITIAL_TAG)

        assertThat(scmActions.getTags(projectFile, INITIAL_TAG))
            .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky

        assertThat(scmActions.getTags(remoteProjectFile, INITIAL_TAG))
            .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky
    }


//    @Nested
//    inner class MultiModuleGitProject :
//        MultiModuleScmProjectBaseIntegrationTest(GitUtils),
//        BaseReleaseTaskIntegrationTest {
//
//        override fun setupRemoteBeforeClone() {
//            super.setupRemoteBeforeClone()
//
//            remoteProjectDir.git("tag", INITIAL_TAG)
//            remoteSubModuleDir.git("tag", INITIAL_TAG)
//
//            // Create dev branch in origin
//            remoteProjectDir.git("branch", ScmConstants.FEATURE_BRANCH)
//            remoteSubModuleDir.git("branch", ScmConstants.FEATURE_BRANCH)
//        }
//
//        @Test
//        fun `test 'release' should fail from release branch when there are acceptable commits on submodule feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteSubModuleDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .buildAndFail()
//
//            // THEN
//            assertFailedRelease(
//                actual = actual,
//                featureBranchCommits = listOf(FIX_COMMIT_MESSAGE),
//            )
//        }
//
//        @Test
//        fun `test 'release' should succeed from release branch when there are acceptable commits on submodule release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteSubModuleDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        @Test
//        fun `test 'release' should fal from feature branch when there are acceptable commits on submodule release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteSubModuleDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .buildAndFail()
//
//            // THEN
//            assertFailedRelease(
//                actual = actual,
//                releaseBranchCommits = listOf(FIX_COMMIT_MESSAGE),
//            )
//        }
//
//        @Test
//        fun `test 'release' should succeed from feature branch when there are acceptable commits on submodule feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteSubModuleDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        override fun assertRepositories(
//            projectDir: File,
//            remoteProjectDir: File,
//            releaseBranchCommits: List<String>,
//            featureBranchCommits: List<String>,
//            lastTag: String
//        ) {
//            super.assertRepositories(projectDir, remoteProjectDir, releaseBranchCommits, featureBranchCommits, lastTag)
//
//            super.assertRepositories(
//                subModuleDir,
//                remoteSubModuleDir,
//                releaseBranchCommits,
//                featureBranchCommits,
//                lastTag
//            )
//        }
//
//    }
//
//    @Nested
//    inner class SingleModuleGitProject :
//        SingleModuleScmProjectBaseIntegrationTest(GitUtils),
//        BaseReleaseTaskIntegrationTest {
//
//        override fun setupRemoteBeforeClone() {
//            super.setupRemoteBeforeClone()
//
//            remoteProjectDir.git("tag", INITIAL_TAG)
//
//            // Create dev branch in origin
//            remoteProjectDir.git("branch", ScmConstants.FEATURE_BRANCH)
//        }
//
//    }
//
//    private interface BaseReleaseTaskIntegrationTest : ScmProjectIntegrationTest {
//
//        @CsvSource(ScmConstants.FEATURE_BRANCH, ScmConstants.RELEASE_BRANCH)
//        @ParameterizedTest
//        fun `test 'release' should fail when there are no acceptable commits`(testBranch: String) {
//            // GIVEN
//            scmUtils.checkout(projectDir, testBranch)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .buildAndFail()
//
//            // THEN
//            assertFailedRelease(actual)
//        }
//
//        @Test
//        fun `test 'release' should fail from release branch when there are acceptable commits on feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .buildAndFail()
//
//            // THEN
//            assertFailedRelease(
//                actual = actual,
//                featureBranchCommits = listOf(FIX_COMMIT_MESSAGE),
//            )
//        }
//
//        @Test
//        fun `test 'release' should succeed from release branch when there are acceptable commits on release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        @Test
//        fun `test 'release' should fal from feature branch when there are acceptable commits on release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .buildAndFail()
//
//            // THEN
//            assertFailedRelease(
//                actual = actual,
//                releaseBranchCommits = listOf(FIX_COMMIT_MESSAGE),
//            )
//        }
//
//        @Test
//        fun `test 'release' should succeed from feature branch when there are acceptable commits on feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME)
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        @Test
//        fun `test 'release with forceRelease' should succeed when there are no acceptable commits`() {
//            // GIVEN
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
//                .build()
//
//            // THEN
//            assertSucceededRelease(
//                actual = actual,
//                releaseBranchCommits = listOf("Release version: $RELEASE_001"),
//                featureBranchCommits = listOf("New SNAPSHOT version: $SNAPSHOT_002", "Release version: $RELEASE_001"),
//            )
//        }
//
//        @Test
//        fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
//                .build()
//
//            // THEN
//            assertSucceededRelease(
//                actual = actual,
//                releaseBranchCommits = listOf("Release version: $RELEASE_001"),
//            )
//        }
//
//        @Test
//        fun `test 'release with forceRelease' should succeed from release branch when there are acceptable commits on release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.RELEASE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        @Test
//        fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on release branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.RELEASE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        @Test
//        fun `test 'release with forceRelease' should succeed from feature branch when there are acceptable commits on feature branch`() {
//            // GIVEN
//            scmUtils.createDummyCommit(remoteProjectDir, ScmConstants.FEATURE_BRANCH, FIX_COMMIT_MESSAGE)
//
//            scmUtils.checkout(projectDir, ScmConstants.FEATURE_BRANCH)
//            scmUtils.fetch(projectDir, ScmConstants.REMOTE)
//
//            // WHEN
//            val actual = createGradleRunner(projectDir, RELEASE_TASK_NAME, "-DforceRelease=true")
//                .build()
//
//            // THEN
//            assertSucceededRelease(actual)
//        }
//
//        fun assertFailedRelease(
//            actual: BuildResult,
//            releaseBranchCommits: List<String> = emptyList(),
//            featureBranchCommits: List<String> = emptyList(),
//        ) {
//            assertThat(actual.output.lines())
//                .contains(
//                    "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
//                    "> There are no acceptable commits.",
//                )
//
//            scmUtils.clean(projectDir)
//            scmUtils.clean(remoteProjectDir)
//
//            // assert project version
//            listOf(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH).forEach {
//                assertThat(getProjectVersion(projectDir, it))
//                    .isEqualTo(SNAPSHOT_001)
//                assertThat(getProjectVersion(remoteProjectDir, it))
//                    .isEqualTo(SNAPSHOT_001)
//            }
//
//            assertRepositories(
//                releaseBranchCommits = releaseBranchCommits,
//                featureBranchCommits = featureBranchCommits,
//            )
//        }
//
//        fun assertSucceededRelease(
//            actual: BuildResult,
//            releaseBranchCommits: List<String> = listOf(
//                "Release version: $RELEASE_001",
//                FIX_COMMIT_MESSAGE,
//            ),
//            featureBranchCommits: List<String> = listOf(
//                "New SNAPSHOT version: $SNAPSHOT_002",
//                "Release version: $RELEASE_001",
//                FIX_COMMIT_MESSAGE,
//            ),
//        ) {
//            assertThat(actual.output.lines())
//                .contains(
//                    "> Task :$SET_RELEASE_VERSION_TASK_NAME",
//                    "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME",
//                    "> Task :build",
//                    "> Task :$SET_SNAPSHOT_VERSION_TASK_NAME",
//                    "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME",
//                    "> Task :$UPDATE_SCM_TASK_NAME",
//                    "> Task :$RELEASE_TASK_NAME",
//                )
//
//            scmUtils.clean(projectDir)
//            scmUtils.clean(remoteProjectDir)
//
//            // assert project version
//            assertThat(getProjectVersion(projectDir, ScmConstants.FEATURE_BRANCH))
//                .isEqualTo(SNAPSHOT_002)
//            assertThat(getProjectVersion(remoteProjectDir, ScmConstants.FEATURE_BRANCH))
//                .isEqualTo(SNAPSHOT_002)
//
//            assertThat(getProjectVersion(projectDir, ScmConstants.RELEASE_BRANCH))
//                .isEqualTo(RELEASE_001)
//            assertThat(getProjectVersion(remoteProjectDir, ScmConstants.RELEASE_BRANCH))
//                .isEqualTo(RELEASE_001)
//
//            assertRepositories(
//                releaseBranchCommits = releaseBranchCommits,
//                featureBranchCommits = featureBranchCommits,
//                lastTag = RELEASE_001.toString(),
//            )
//        }
//
//        fun assertRepositories(
//            projectDir: File = this@BaseReleaseTaskIntegrationTest.projectDir,
//            remoteProjectDir: File = this@BaseReleaseTaskIntegrationTest.remoteProjectDir,
//            releaseBranchCommits: List<String> = emptyList(),
//            featureBranchCommits: List<String> = emptyList(),
//            lastTag: String = INITIAL_TAG,
//        ) {
//            // assert commits
//            assertThat(
//                scmUtils.getCommits(
//                    projectDir,
//                    INITIAL_TAG,
//                    "${ScmConstants.REMOTE}/${ScmConstants.RELEASE_BRANCH}"
//                )
//            )
//                .containsExactlyElementsOf(releaseBranchCommits)
//            assertThat(
//                scmUtils.getCommits(
//                    projectDir,
//                    INITIAL_TAG,
//                    "${ScmConstants.REMOTE}/${ScmConstants.FEATURE_BRANCH}"
//                )
//            )
//                .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
//            assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, ScmConstants.RELEASE_BRANCH))
//                .containsExactlyElementsOf(releaseBranchCommits)
//            assertThat(scmUtils.getCommits(projectDir, INITIAL_TAG, ScmConstants.FEATURE_BRANCH))
//                .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
//
//            assertThat(scmUtils.getCommits(remoteProjectDir, INITIAL_TAG, ScmConstants.RELEASE_BRANCH))
//                .containsExactlyElementsOf(releaseBranchCommits)
//            assertThat(scmUtils.getCommits(remoteProjectDir, INITIAL_TAG, ScmConstants.FEATURE_BRANCH))
//                .containsExactlyInAnyOrderElementsOf(featureBranchCommits) // flaky
//
//            // assert tags
//            assertThat(scmUtils.getLastTag(projectDir, ScmConstants.RELEASE_BRANCH))
//                .isEqualTo(lastTag)
//
//            assertThat(scmUtils.getLastTag(remoteProjectDir, ScmConstants.RELEASE_BRANCH))
//                .isEqualTo(lastTag)
//
//            val expectedTags = setOf(lastTag, INITIAL_TAG)
//
//            assertThat(scmUtils.getTags(projectDir))
//                .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky
//
//            assertThat(scmUtils.getTags(remoteProjectDir))
//                .containsExactlyInAnyOrderElementsOf(expectedTags) // flaky
//        }
//
//    }

}

