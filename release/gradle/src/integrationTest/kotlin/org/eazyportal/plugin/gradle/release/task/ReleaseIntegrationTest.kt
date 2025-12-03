package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.common.GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.FIX_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_TAG
import org.eazyportal.plugin.gradle.release.TestCaseBuilder
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.io.File

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ReleaseIntegrationTest {

//    private lateinit var testCase: TestCaseBuilder.Given<BaseScmProjectTestCase>
//
//    @BeforeAll
//    fun initialize() {
//
//    }

    //    @Order(1)
    @Test
    fun test(@TempDir workingDir: File) {
        val testCase = givenTestCase(MultiModuleTrunkFlowScmProjectTestCase::class, workingDir) {
            configureProject()
        }

        // Validate status before running anything
        testCase.whenGradleTaskSucceeds("clean")
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                    projectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                assertThat(scmActions.getCurrentBranch(submoduleProjectFile))
                    .isEqualTo("HEAD") // after cloning, submodule is detached

                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                    projectFile,
                ).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                it.scmStatus(submoduleProjectFile) {
                    // after cloning, submodule is detached
                    first().matches { firstLine -> firstLine.startsWith("HEAD detached at") }

                    contains("nothing to commit, working tree clean")
                }

                it.scmCommits(remoteProjectFile) {
                    containsExactly(
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }
                it.scmCommits(remoteSubmoduleProjectFile) {
                    containsExactly(
                        FIX_COMMIT_MESSAGE,
                        "initial commit"
                    )
                }

                // local misses commits because `clone` happened before these commits
                it.scmCommits(projectFile) {
                    containsExactly(
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }
                it.scmCommits(submoduleProjectFile) {
                    containsExactly("initial commit")
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_001)
                }
            }

        testCase.whenGradleTaskSucceeds(PREPARE_REPOSITORY_FOR_RELEASE_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                assertThat(scmActions.getCurrentBranch(projectFile))
                    .isEqualTo(scmConfig.featureBranch)
                assertThat(scmActions.getCurrentBranch(submoduleProjectFile))
                    .isEqualTo("HEAD") // after cloning submodule is detached

                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                    projectFile,
                ).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                it.scmStatus(submoduleProjectFile) {
                    // after cloning, submodule is detached
                    first().matches { firstLine -> firstLine.startsWith("HEAD detached from") }

                    contains("nothing to commit, working tree clean")
                }

                listOf(remoteProjectFile, projectFile).forEach { project ->
                    it.scmCommits(project) {
                        containsExactly(
                            "chore: include $SUBMODULE_NAME changes",
                            FIX_COMMIT_MESSAGE,
                            "chore: add $SUBMODULE_NAME submodule",
                            "initial commit"
                        )
                    }
                }
                listOf(remoteSubmoduleProjectFile, submoduleProjectFile).forEach { project ->
                    it.scmCommits(project) {
                        containsExactly(
                            FIX_COMMIT_MESSAGE,
                            "initial commit",
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_001)
                }
            }

        testCase.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                listOf(
                    projectFile,
                    submoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.featureBranch)
                }

                listOf(remoteProjectFile, remoteSubmoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                        "Changes not staged for commit:",
                        "modified:   $SUBMODULE_NAME (modified content)",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }
                it.scmStatus(submoduleProjectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                        "Changes not staged for commit:",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }

                it.scmCommits(projectFile) {
                    containsExactly(
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }

                it.scmCommits(submoduleProjectFile) {
                    containsExactly(
                        FIX_COMMIT_MESSAGE,
                        "initial commit",
                    )
                }

                it.projectVersion {
                    isEqualTo(RELEASE_001)
                }
            }

        testCase.whenGradleTaskSucceeds(FINALIZE_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                listOf(
                    projectFile,
                    submoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.featureBranch)
                }

                listOf(remoteProjectFile, remoteSubmoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                listOf(projectFile, submoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }

                it.scmCommits(projectFile) {
                    containsExactly(
                        "Release version: $RELEASE_001",
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }

                it.scmCommits(submoduleProjectFile) {
                    containsExactly(
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                        "initial commit",
                    )
                }

                it.projectVersion {
                    isEqualTo(RELEASE_001)
                }
            }

        // simulate real execution
        testCase.whenGradleTaskSucceeds("build")

        testCase.whenGradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                listOf(
                    projectFile,
                    submoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }

                listOf(remoteProjectFile, remoteSubmoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                        "Changes not staged for commit:",
                        "modified:   $SUBMODULE_NAME (modified content)",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }
                it.scmStatus(submoduleProjectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                        "Changes not staged for commit:",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }

                it.scmCommits(projectFile) {
                    containsExactly(
                        "Release version: $RELEASE_001",
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }

                it.scmCommits(submoduleProjectFile) {
                    containsExactly(
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                        "initial commit",
                    )
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }

        testCase.whenGradleTaskSucceeds(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                listOf(
                    projectFile,
                    submoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }

                listOf(remoteProjectFile, remoteSubmoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                listOf(projectFile, submoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }

                it.scmCommits(projectFile) {
                    containsExactly(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }

                it.scmCommits(submoduleProjectFile) {
                    containsExactly(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                        "initial commit",
                    )
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }

        testCase.whenGradleTaskSucceeds(UPDATE_SCM_TASK_NAME)
            .thenAssert {
                listOf(
                    remoteProjectFile,
                    remoteSubmoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }
                listOf(
                    projectFile,
                    submoduleProjectFile,
                ).forEach { project ->
                    assertThat(scmActions.getCurrentBranch(project))
                        .isEqualTo(scmConfig.releaseBranch)
                }

                // Workaround for using none-bare repository as origin
                scmActions.fetch(remoteProjectFile.resolve(SUBMODULE_NAME), scmConfig.remote, scmConfig.releaseBranch, scmConfig.featureBranch)

                listOf(remoteProjectFile, remoteSubmoduleProjectFile).forEach { project ->
                    // Workaround for using none-bare repository as origin
                    scmActions.clean(project)

                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }
                listOf(projectFile, submoduleProjectFile).forEach { project ->
                    it.scmStatus(project) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "nothing to commit, working tree clean",
                        )
                    }
                }

                it.scmCommits(projectFile) {
                    containsExactly(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        "chore: include $SUBMODULE_NAME changes",
                        FIX_COMMIT_MESSAGE,
                        "chore: add $SUBMODULE_NAME submodule",
                        "initial commit"
                    )
                }

                it.scmCommits(submoduleProjectFile) {
                    containsExactly(
                        "New SNAPSHOT version: $SNAPSHOT_002",
                        "Release version: $RELEASE_001",
                        FIX_COMMIT_MESSAGE,
                        "initial commit",
                    )
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }
    }

    private fun BaseMultiModuleScmProjectTestCase.configureProject() {
        scmActions.tag(remoteProjectFile, INITIAL_VERSION)
        scmActions.tag(remoteSubmoduleProjectFile, INITIAL_VERSION)

        scmActions.checkout(remoteProjectFile, scmConfig.featureBranch)
        createAndCommitDummyFile(remoteProjectFile, FIX_COMMIT_MESSAGE)

        scmActions.checkout(remoteSubmoduleProjectFile, scmConfig.featureBranch)
        createAndCommitDummyFile(remoteSubmoduleProjectFile, FIX_COMMIT_MESSAGE)

        // Workaround for using none-bare repository as origin (fetch submodule changes)
        scmActions.fetch(remoteProjectFile.resolve(SUBMODULE_NAME), scmConfig.remote)

        scmActions.add(remoteProjectFile, ".")
        scmActions.commit(remoteProjectFile, "chore: include $SUBMODULE_NAME changes")
    }

    companion object {
        private val INITIAL_VERSION = Version.of(INITIAL_TAG)
    }

}
