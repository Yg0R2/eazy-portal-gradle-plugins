package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.SET_SNAPSHOT_VERSION_TASK_NAME
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.SNAPSHOT_002
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class SetSnapshotVersionTaskIntegrationTest<T : BaseScmProjectTestCase> {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when project version is not release version`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            setProjectVersion(SNAPSHOT_001)
        }.whenGradleTaskFails(SET_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                        "> Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version.",
                    )
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_001)
                }
            }
    }

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should set snapshot version`(
        testCaseClass: KClass<T>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            setProjectVersion(RELEASE_001)
        }.whenGradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }
    }

}
