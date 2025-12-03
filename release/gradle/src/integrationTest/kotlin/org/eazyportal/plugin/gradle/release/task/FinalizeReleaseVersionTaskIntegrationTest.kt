package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.FINALIZE_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class FinalizeReleaseVersionTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should finalize release version`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
            }

            setProjectVersion(RELEASE_001)
        }.whenGradleTaskSucceeds(FINALIZE_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME")
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.featureBranch}",
                        "Your branch is ahead of '${scmConfig.remote}/${scmConfig.featureBranch}' by 1 commit.",
                        "nothing to commit, working tree clean"
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.featureBranch}' by 1 commit.",
                            "nothing to commit, working tree clean"
                        )
                    }

                    it.scmCommits(projectFile) {
                        containsExactly(
                            "Release version: $RELEASE_001",
                            "chore: add $SUBMODULE_NAME submodule",
                            "initial commit",
                        )
                    }

                    it.scmCommits(submoduleProjectFile) {
                        containsExactly(
                            "Release version: $RELEASE_001",
                            "initial commit",
                        )
                    }
                } else {
                    it.scmLocalCommits {
                        containsExactly(
                            "Release version: $RELEASE_001",
                            "initial commit",
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(RELEASE_001)
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when there is nothing to commit`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
            }
        }.whenGradleTaskFails(FINALIZE_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "> Task :$FINALIZE_RELEASE_VERSION_TASK_NAME FAILED",
                        "nothing to commit, working tree clean",
                    )
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.featureBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                        "nothing to commit, working tree clean"
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "nothing to commit, working tree clean"
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_001)
                }
            }
    }

}
