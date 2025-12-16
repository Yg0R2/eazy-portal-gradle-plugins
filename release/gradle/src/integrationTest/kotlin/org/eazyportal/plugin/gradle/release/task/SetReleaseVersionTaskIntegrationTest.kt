package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_ADD_SUBMODULES_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.FIX_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_COMMIT_MESSAGE
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.testcase.MultiModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.MultiModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.SingleModuleCustomizedScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.SingleModuleGitFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.SingleModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.MultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.SingleModuleScmProjectTestCase
import org.eazyportal.plugin.release.core.TestGitActions.Companion.TEST_GIT_ACTIONS
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SetReleaseVersionTaskIntegrationTest {

    interface SetReleaseVersionTaskTestCase {

        fun `test 'run' should fail when there are no acceptable commits`(): List<DynamicTest>

        fun `test 'run' should succeed when release is forced but there are no acceptable commits`(): List<DynamicTest>

        fun `test 'run' from release branch should succeed when there are acceptable commits on release branch`()

        fun `test 'run' from release branch should fail when there are acceptable commits on feature branch`()

        fun `test 'run' from feature branch should succeed when there are acceptable commits on release branch`()

        fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`()

    }

    interface SetReleaseVersionTaskSingleModuleTestCase :
        SetReleaseVersionTaskTestCase,
        SingleModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there are no acceptable commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from '$it' branch" }
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$SET_RELEASE_VERSION_TASK_NAME FAILED",
                            "Ignoring missing tag from release version calculation.",
                            "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                            "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                            "> There are no acceptable commits.",
                        )
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should succeed when release is forced but there are no acceptable commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from '$it' branch" }
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :setReleaseVersion",
                            "Ignoring missing tag from release version calculation.",
                            "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                        )
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                    }

                    projectVersionIn(projectDir.localProjectFile, RELEASE_001)
                }
            }

        @Test
        override fun `test 'run' from release branch should succeed when there are acceptable commits on release branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                        createAndCommitDummyFile(projectDir.localProjectFile, FIX_COMMIT_MESSAGE)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            FIX_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    projectVersionIn(projectDir.localProjectFile, RELEASE_001)
                }
            }
        }

        @Test
        override fun `test 'run' from release branch should fail when there are acceptable commits on feature branch`() {
            assumeTrue(this !is SingleModuleTrunkFlowScmProjectTestCase) {
                "Test is not valid for Trunk Based SCM flow"
            }

            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        createAndCommitDummyFile(projectDir.localProjectFile, FIX_COMMIT_MESSAGE)

                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$SET_RELEASE_VERSION_TASK_NAME FAILED",
                            "Ignoring missing tag from release version calculation.",
                            "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                            "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                            "> There are no acceptable commits.",
                        )
                    }
                }
            }
        }

        @Test // TODO: fix implementation
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on release branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                        createAndCommitDummyFile(projectDir.localProjectFile, FIX_COMMIT_MESSAGE)

                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes to be committed:",
                            "new file:   $DUMMY_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                    }

                    projectVersionIn(projectDir.localProjectFile, RELEASE_001)
                }
            }
        }

        @Test
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        createAndCommitDummyFile(projectDir.localProjectFile, FIX_COMMIT_MESSAGE)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes to be committed:",
                            "new file:   $DUMMY_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(INITIAL_COMMIT_MESSAGE)
                    }

                    projectVersionIn(projectDir.localProjectFile, RELEASE_001)
                }
            }
        }

    }

    interface SetReleaseVersionTaskMultiModuleTestCase :
        SetReleaseVersionTaskTestCase,
        MultiModuleScmProjectTestCase {

        @TestFactory
        override fun `test 'run' should fail when there are no acceptable commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from '$it' branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$SET_RELEASE_VERSION_TASK_NAME FAILED",
                            *IntRange(0, SUBMODULE_NAMES.size + 1).flatMap {
                                listOf(
                                    "Ignoring missing tag from release version calculation.",
                                    "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                                )
                            }.toTypedArray(),
                            "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                            "> There are no acceptable commits.",
                        )
                    }
                }
            }

        @TestFactory
        override fun `test 'run' should succeed when release is forced but there are no acceptable commits`(): List<DynamicTest> =
            runDynamicTestCase(
                listOf(scmConfig.releaseBranch, scmConfig.featureBranch),
                { "from $it branch" },
            ) { testBranch ->
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, testBranch)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }
                }
            }

        @Test
        override fun `test 'run' from release branch should succeed when there are acceptable commits on release branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (new commits, modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(
                                FIX_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }
                }
            }
        }

        @Test
        override fun `test 'run' from release branch should fail when there are acceptable commits on feature branch`() {
            assumeTrue(this !is MultiModuleTrunkFlowScmProjectTestCase) {
                "Test is not valid for Trunk Based SCM flow"
            }

            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }

                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)
                    }
                }

                whenExecute {
                    gradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains(
                            "> Task :$SET_RELEASE_VERSION_TASK_NAME FAILED",
                            *IntRange(0, SUBMODULE_NAMES.size + 1).flatMap {
                                listOf(
                                    "Ignoring missing tag from release version calculation.",
                                    "Ignoring invalid commit: $INITIAL_COMMIT_MESSAGE",
                                )
                            }.toTypedArray(),
                            "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                            "> There are no acceptable commits.",
                        )
                    }
                }
            }
        }

        @Test // TODO: fix implementation
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on release branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }

                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes to be committed:",
                                "new file:   $DUMMY_FILE_NAME",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }

                }
            }
        }

        @Test
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes to be committed:",
                                "new file:   $DUMMY_FILE_NAME",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(INITIAL_COMMIT_MESSAGE)
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }

                }
            }
        }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase :
        SetReleaseVersionTaskSingleModuleTestCase,
        SingleModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class SingleModuleTrunkFlowTestCase :
        SetReleaseVersionTaskSingleModuleTestCase,
        SingleModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS) {

        @Test
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        createAndCommitDummyFile(projectDir.localProjectFile, FIX_COMMIT_MESSAGE)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    scmStatusIn(projectDir.localProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }

                    scmCommitsIn(projectDir.localProjectFile) {
                        containsExactly(
                            FIX_COMMIT_MESSAGE,
                            INITIAL_COMMIT_MESSAGE,
                        )
                    }

                    projectVersionIn(projectDir.localProjectFile, RELEASE_001)
                }
            }
        }

    }

    @Nested
    inner class SingleModuleCustomizedTestCase :
        SetReleaseVersionTaskSingleModuleTestCase,
        SingleModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class MultiModuleGitFlowTestCase :
        SetReleaseVersionTaskMultiModuleTestCase,
        MultiModuleGitFlowScmProjectTestCase(TEST_GIT_ACTIONS)

    @Nested
    inner class MultiModuleTrunkFlowTestCase :
        SetReleaseVersionTaskMultiModuleTestCase,
        MultiModuleTrunkFlowScmProjectTestCase(TEST_GIT_ACTIONS) {

        @Test
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on release branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.releaseBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }

                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (new commits, modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(
                                FIX_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }

                }
            }
        }

        @Test
        override fun `test 'run' from feature branch should succeed when there are acceptable commits on feature branch`() {
            runTestCase {
                givenTestCase {
                    withScmProject {
                        scmActions.checkout(projectDir.localProjectFile, scmConfig.featureBranch)

                        submoduleProjectDirs.forEach {
                            createAndCommitDummyFile(it.localProjectFile, FIX_COMMIT_MESSAGE)
                        }
                    }
                }

                whenExecute {
                    gradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
                }

                thenVerify {
                    gradleTaskOutput {
                        contains("> Task :$SET_RELEASE_VERSION_TASK_NAME")
                    }

                    with(projectDir.localProjectFile) {
                        scmStatusIn(this) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                                "Changes not staged for commit:",
                                *SUBMODULE_NAMES.map { "modified:   $it (new commits, modified content)" }
                                    .toTypedArray(),
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(this) {
                            containsExactly(
                                CHORE_ADD_SUBMODULES_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(this, RELEASE_001)
                    }

                    submoduleProjectDirs.forEach {
                        scmStatusIn(it.localProjectFile) {
                            contains(
                                "On branch ${scmConfig.releaseBranch}",
                                "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                                "Changes not staged for commit:",
                                "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                                "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                            )
                        }

                        scmCommitsIn(it.localProjectFile) {
                            containsExactly(
                                FIX_COMMIT_MESSAGE,
                                INITIAL_COMMIT_MESSAGE,
                            )
                        }

                        projectVersionIn(it.localProjectFile, RELEASE_001)
                    }

                }
            }
        }

    }

    @Nested
    inner class MultiModuleCustomizedTestCase :
        SetReleaseVersionTaskMultiModuleTestCase,
        MultiModuleCustomizedScmProjectTestCase(TEST_GIT_ACTIONS)

}
