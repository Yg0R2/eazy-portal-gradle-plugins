package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.FEATURE_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_RELEASE_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_010
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_100
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class SetReleaseVersionTaskIntegrationTest<T : BaseScmProjectTestCase> {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when there are no acceptable commits`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)
        }.whenGradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Ignoring missing Git tag from release version calculation.",
                        "Ignoring invalid commit: initial commit",
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
                    )
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_001)
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should set release version`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if ((this is SingleModuleCustomizedProjectTestCase) || (this is MultiModuleCustomizedProjectTestCase)) {
                createAndCommitDummyFile(projectFile, "dummy: configure custom ConventionalCommitTypes")
            } else {
                createAndCommitDummyFile(projectFile, FEATURE_COMMIT_MESSAGE)
            }
        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Ignoring missing Git tag from release version calculation.",
                        "Ignoring invalid commit: initial commit",
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                    )
                }

                if ((this is SingleModuleCustomizedProjectTestCase) || (this is MultiModuleCustomizedProjectTestCase)) {
                    it.projectVersion {
                        isEqualTo(RELEASE_100)
                    }
                } else {
                    it.projectVersion {
                        isEqualTo(RELEASE_010)
                    }
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should set release version when release is forced`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)
        }.whenGradleTaskSucceeds(SET_RELEASE_VERSION_TASK_NAME, "-DforceRelease=true")
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Ignoring missing Git tag from release version calculation.",
                        "Ignoring invalid commit: initial commit",
                        "> Task :$SET_RELEASE_VERSION_TASK_NAME",
                    )
                }

                it.projectVersion {
                    isEqualTo(RELEASE_001)
                }
            }
    }

}
