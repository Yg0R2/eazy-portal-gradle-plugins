package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.junit.classNamed
import org.eazyportal.plugin.gradle.release.MultiModuleScmProjectBaseIntegrationTest1
import org.eazyportal.plugin.gradle.release.SingleModuleScmProjectBaseIntegrationTest1
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleCustomFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleCustomFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class PrepareRepositoryForReleaseTaskIntegrationTest {

    @MethodSource("shouldCleanLocalChangesTestCases")
    @ParameterizedTest
    fun <T : BaseScmProjectTestCase> `test 'run' should clean local commits`(
        testCaseClass: KClass<T>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, testBranch)

            createAndCommitDummyFile(projectFile, CHORE_COMMIT_MESSAGE)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, testBranch)

                createAndCommitDummyFile(submoduleProjectFile, CHORE_COMMIT_MESSAGE)
            }
        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                }

                it.scmLocalCommits {
                    doesNotContain(CHORE_COMMIT_MESSAGE)
                }
            }
    }

    @MethodSource("shouldCleanLocalChangesTestCases")
    @ParameterizedTest
    fun <T : BaseScmProjectTestCase> `test 'run' should clean local file changes`(
        testCaseClass: KClass<T>,
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

                if (this is MultiModuleGitFlowScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "HEAD detached at refs/heads/$testBranch",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
            }
    }

    @MethodSource("shouldCleanLocalChangesTestCases")
    @ParameterizedTest
    fun <T : BaseScmProjectTestCase> `test 'run' should pull remote changes`(
        testCaseClass: KClass<T>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(remoteProjectFile, scmConfig.releaseBranch)
            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.releaseBranch}")

            scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
            createAndCommitDummyFile(remoteProjectFile, "chore: commit on ${scmConfig.featureBranch}")

            scmActions.checkout(projectFile, testBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.releaseBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.releaseBranch}")

                scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
                createAndCommitDummyFile(remoteSubmoduleProjectFile, "chore: commit on ${scmConfig.featureBranch}")

                scmActions.checkout(submoduleProjectFile, testBranch)
            }
        }.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")
                }

                // TODO: fix Trunk
                assertThat(
                    scmActions.getCommits(projectFile, scmConfig.releaseBranch, scmConfig.featureBranch)
                ).contains("chore: commit on ${scmConfig.featureBranch}")
                    .containsExactlyElementsOf(
                        scmActions.getCommits(remoteProjectFile, scmConfig.releaseBranch, scmConfig.featureBranch)
                    )

                // TODO: fix Trunk
                assertThat(
                    scmActions.getCommits(projectFile, scmConfig.featureBranch, scmConfig.releaseBranch)
                ).contains("chore: commit on ${scmConfig.releaseBranch}")
                    .containsExactlyElementsOf(
                        scmActions.getCommits(remoteProjectFile, scmConfig.featureBranch, scmConfig.releaseBranch)
                    )
            }
    }

    //    private class TestArguments<T : BaseScmProjectTestCase>(
//        private val testCaseClass: KClass<T>,
//        private val testBranch: String,
//        private val projectFileProvider: T.() -> ProjectFile<File> = { projectFile },
//        private val remoteProjectFileProvider: T.() -> ProjectFile<File> = { remoteProjectFile },
//    ) : Arguments {
//
//        override fun get(): Array<out Any?> =
//            arrayOf(
//                testCaseClass,
//                testBranch,
//                projectFileProvider,
//                remoteProjectFileProvider,
//            )
//
//        override fun toString(): String =
//            testCaseClass.java.simpleName
//
//    }
//
    companion object {
        @JvmStatic
        private fun shouldCleanLocalChangesTestCases(): List<Arguments> =
            listOf(
                Arguments.of(
                    classNamed(SingleModuleGitFlowScmProjectTestCase::class),
                    ScmConstants.RELEASE_BRANCH,
                ),
                Arguments.of(
                    classNamed(SingleModuleGitFlowScmProjectTestCase::class),
                    ScmConstants.FEATURE_BRANCH,
                ),
                Arguments.of(
                    classNamed(MultiModuleGitFlowScmProjectTestCase::class),
                    ScmConstants.RELEASE_BRANCH,
                ),
                Arguments.of(
                    classNamed(MultiModuleGitFlowScmProjectTestCase::class),
                    ScmConstants.FEATURE_BRANCH,
                ),

                Arguments.of(
                    classNamed(SingleModuleTrunkFlowScmProjectTestCase::class),
                    ScmConstants.RELEASE_BRANCH,
                ),
                Arguments.of(
                    classNamed(MultiModuleTrunkFlowScmProjectTestCase::class),
                    ScmConstants.RELEASE_BRANCH,
                ),

                Arguments.of(
                    classNamed(SingleModuleCustomFlowScmProjectTestCase::class),
                    "dummy-release-branch",
                ),
                Arguments.of(
                    classNamed(SingleModuleCustomFlowScmProjectTestCase::class),
                    "dummy-feature-branch",
                ),
                Arguments.of(
                    classNamed(MultiModuleCustomFlowScmProjectTestCase::class),
                    "dummy-release-branch",
                ),
                Arguments.of(
                    classNamed(MultiModuleCustomFlowScmProjectTestCase::class),
                    "dummy-feature-branch",
                ),
            )

    }


    @Nested
    inner class MultiModuleGitFlow : MultiModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {

        override fun setUpBeforeClone() {
            // Create remote feature branch
            scmActions.execute(remoteProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
            scmActions.execute(remoteSubmoduleProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local changes`(testBranch: String) {
            // GIVEN
            scmActions.checkout(projectFile, testBranch)

            createDummyFile(submoduleProjectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch $testBranch",
                    "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                    "nothing to commit, working tree clean",
                )
        }

    }

    @Nested
    inner class MultiModuleTrunkFlow : MultiModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {

        override fun initializeRepository(initProjectFile: ProjectFile<File>) {
            GradleProjectBuilder(
                projectDir = initProjectFile.getFile(),
                projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
            ).withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).build()

            scmActions.initializeRepository(initProjectFile)
        }

        @Test
        fun `test 'run' should clean local changes`() {
            // GIVEN
            createDummyFile(submoduleProjectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch ${scmConfig.releaseBranch}",
                    "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                    "nothing to commit, working tree clean",
                )
        }

    }

    @Nested
    inner class SingleModuleGitFlow : SingleModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {

        override fun setUpBeforeClone() {
            // Create remote feature branch
            scmActions.execute(remoteProjectFile, "branch", ScmConstants.FEATURE_BRANCH)
        }

        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
        @ParameterizedTest
        fun `test 'run' should clean local changes`(testBranch: String) {
            // GIVEN
            scmActions.checkout(projectFile, testBranch)

            createDummyFile(projectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch $testBranch",
                    "Your branch is up to date with '${scmConfig.remote}/$testBranch'.",
                    "nothing to commit, working tree clean",
                )
        }
    }

    @Nested
    inner class SingleModuleTrunkFlow : SingleModuleScmProjectBaseIntegrationTest1(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {

        override fun initializeRepository(initProjectFile: ProjectFile<File>) {
            GradleProjectBuilder(
                projectDir = initProjectFile.getFile(),
                projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
            ).withExtraProjectConfig(
                """
                eazyRelease {
                    scmConfig = org.eazyportal.plugin.release.core.scm.model.ScmConfig.TRUNK_BASED_FLOW
                }
                """.trimIndent()
            ).build()

            scmActions.initializeRepository(initProjectFile)
        }

        @Test
        fun `test 'run' should clean local changes`() {
            // GIVEN
            createDummyFile(projectFile)

            // WHEN
            val actual = createGradleRunner(projectFile.getFile(), PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
                .build()

            // THEN
            assertThat(actual.output.lines())
                .contains("> Task :$PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME")

            assertThat(scmActions.status(projectFile))
                .contains(
                    "On branch ${scmConfig.releaseBranch}",
                    "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                    "nothing to commit, working tree clean",
                )
        }

//        @CsvSource(ScmConstants.RELEASE_BRANCH, ScmConstants.FEATURE_BRANCH)
//        @ParameterizedTest
//        fun `test 'run' update release and feature branches`(testBranch: String) {
//            // GIVEN
//            scmUtils.createDummyCommit(
//                remoteProjectFile,
//                ScmConstants.RELEASE_BRANCH,
//                "chore: commit on ${ScmConstants.RELEASE_BRANCH}"
//            )
//            scmUtils.createDummyCommit(
//                remoteProjectFile,
//                ScmConstants.FEATURE_BRANCH,
//                "chore: commit on ${ScmConstants.FEATURE_BRANCH}"
//            )
//
//            scmUtils.checkout(projectDir, testBranch)
//
//            // WHEN
//            val actual =
//                createGradleRunner(projectDir, PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
//                    .build()
//
//            // THEN
//            assertThat(actual.output.lines())
//                .contains("> Task :${PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME}")
//
//            assertThat(
//                scmUtils.getCommits(
//                    remoteProjectFile,
//                    ScmConstants.RELEASE_BRANCH,
//                    ScmConstants.FEATURE_BRANCH,
//                )
//            ).contains("chore: commit on ${ScmConstants.FEATURE_BRANCH}")
//                .containsExactlyElementsOf(
//                    scmUtils.getCommits(
//                        projectDir,
//                        ScmConstants.RELEASE_BRANCH,
//                        ScmConstants.FEATURE_BRANCH,
//                    )
//                )
//
//            assertThat(
//                scmUtils.getCommits(
//                    remoteProjectFile,
//                    ScmConstants.FEATURE_BRANCH,
//                    ScmConstants.RELEASE_BRANCH,
//                )
//            ).contains("chore: commit on ${ScmConstants.RELEASE_BRANCH}")
//                .containsExactlyElementsOf(
//                    scmUtils.getCommits(
//                        projectDir,
//                        ScmConstants.FEATURE_BRANCH,
//                        ScmConstants.RELEASE_BRANCH,
//                    )
//                )
//        }

    }

}
