package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.GradleTestFixtures.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.FEATURE_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.MultiModuleTrunkFlowScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleCustomizedProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.SingleModuleTrunkFlowScmProjectTestCase
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

class SetReleaseVersionTaskIntegrationTest {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should fail when there are no acceptable commits`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, scmConfig.featureBranch)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, scmConfig.featureBranch)
            }
        }.whenGradleTaskFails(SET_RELEASE_VERSION_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains(
                        "Ignoring missing Git tag from release version calculation.",
                        "Ignoring invalid commit: initial commit",
                        "Execution failed for task ':$SET_RELEASE_VERSION_TASK_NAME'.",
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

    // TODO: test 'run' should fail from release branch when feature branch has acceptable commits
    // TODO: test 'run' should succeed from release branch when release branch has acceptable commits
    // TODO: test 'run' should succeed from feature branch when feature branch has acceptable commits
    // TODO: test 'run' should succeed from feature branch when release branch has acceptable commits

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCases")
    @ParameterizedTest
    fun `test 'run' should set release version`(
        testCaseClass: KClass<BaseScmProjectTestCase>,
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

                if ((this is SingleModuleTrunkFlowScmProjectTestCase) || (this is MultiModuleTrunkFlowScmProjectTestCase)) {
                    it.scmStatus(projectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is ahead of '${scmConfig.remote}/${scmConfig.releaseBranch}' by 1 commit.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }
                } else {
                    it.scmStatus(projectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes to be committed:",
                            "new file:   $DUMMY_FILE_NAME",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        )
                    }
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }
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
        testCaseClass: KClass<BaseScmProjectTestCase>,
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

                it.scmStatus(projectFile) {
                    contains(
                        "On branch ${scmConfig.releaseBranch}",
                        "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                        "Changes not staged for commit:",
                        "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                        "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                    )
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmStatus(submoduleProjectFile) {
                        contains(
                            "On branch ${scmConfig.releaseBranch}",
                            "Your branch is up to date with '${scmConfig.remote}/${scmConfig.releaseBranch}'.",
                            "Changes not staged for commit:",
                            "modified:   $GRADLE_PROPERTIES_FILE_NAME",
                            "no changes added to commit (use \"git add\" and/or \"git commit -a\")",
                        )
                    }
                }

                it.projectVersion {
                    isEqualTo(RELEASE_001)
                }
            }
    }

}
