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

class FinalizeSnapshotVersionTaskIntegrationTest<T : BaseScmProjectTestCase> {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when there is nothing to commit`(
        testCaseClass: KClass<T>,
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
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should finalize snapshot version`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            setProjectVersion(SNAPSHOT_002)
        }.whenGradleTaskSucceeds(FINALIZE_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$FINALIZE_SNAPSHOT_VERSION_TASK_NAME")
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
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
            }
    }

}
