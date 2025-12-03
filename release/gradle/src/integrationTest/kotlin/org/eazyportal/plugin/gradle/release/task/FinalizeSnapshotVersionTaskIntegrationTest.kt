package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class FinalizeSnapshotVersionTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when there is nothing to commit`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir)
            .whenGradleTaskFails(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME FAILED",
                        "nothing to commit, working tree clean",
                    )
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                        "nothing to commit, working tree clean"
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "nothing to commit, working tree clean"
                        )
                    }
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should finalize snapshot version`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            setProjectVersion(SNAPSHOT_002)
        }.whenGradleTaskSucceeds(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME")
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                        "nothing to commit, working tree clean"
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                            "nothing to commit, working tree clean"
                        )
                    }

                    it.scmCommits(projectFile) {
                        containsExactly(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "chore: add $SUBMODULE_NAME submodule",
                            "initial commit",
                        )
                    }

                    it.scmCommits(submoduleProjectFile) {
                        containsExactly(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "initial commit",
                        )
                    }
                } else {
                    it.scmLocalCommits {
                        containsExactly(
                            "New SNAPSHOT version: $SNAPSHOT_002",
                            "initial commit",
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }
    }

}
