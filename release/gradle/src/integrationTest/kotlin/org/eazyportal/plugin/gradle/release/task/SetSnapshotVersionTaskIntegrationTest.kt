package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
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

class SetSnapshotVersionTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when project version is not release version`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
            }

            setProjectVersion(SNAPSHOT_001)
        }.whenGradleTaskFails(SET_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Execution failed for task ':$SET_SNAPSHOT_VERSION_TASK_NAME'.",
                        "> Project already on ${Version.DEVELOPMENT_VERSION_SUFFIX} version.",
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

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should set snapshot version`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
            }

            setProjectVersion(RELEASE_001)
        }.whenGradleTaskSucceeds(SET_SNAPSHOT_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$SET_SNAPSHOT_VERSION_TASK_NAME")
                }

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.featureBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                        "Changes not staged for commit:",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.featureBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.featureBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(SNAPSHOT_002)
                }
            }
    }

}
