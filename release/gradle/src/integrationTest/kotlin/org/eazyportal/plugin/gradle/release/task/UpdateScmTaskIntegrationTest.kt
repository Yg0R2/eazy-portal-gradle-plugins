package org.eazyportal.plugin.gradle.release.task

import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.gradle.release.TestCaseBuilder.givenTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.task.EazyReleaseTaskConstants.UPDATE_SCM_TASK_NAME
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.reflect.KClass

class UpdateScmTaskIntegrationTest<T : BaseScmProjectTestCase> {

    @MethodSource("org.eazyportal.plugin.gradle.release.asd.BaseProjectTestCase#testCasesWithBranch")
    @ParameterizedTest
    fun `test 'run' should update SCM with commits`(
        testCaseClass: KClass<T>,
        testBranch: String,
        @TempDir workingDir: File,
    ) {
        givenTestCase(testCaseClass, workingDir) {
            scmActions.checkout(projectFile, testBranch)

            createAndCommitDummyFile(projectFile)

            if (this is BaseMultiModuleScmProjectTestCase) {
                scmActions.checkout(submoduleProjectFile, testBranch)

                createAndCommitDummyFile(submoduleProjectFile)
            }
        }.whenGradleTaskSucceeds(UPDATE_SCM_TASK_NAME)
            .thenAssert {
                it.taskOutput {
                    contains("> Task :$UPDATE_SCM_TASK_NAME")
                }

                it.scmCommits(projectFile) {
                    contains(CHORE_COMMIT_MESSAGE)
                }

                if (this is BaseMultiModuleScmProjectTestCase) {
                    it.scmCommits(submoduleProjectFile) {
                        contains(CHORE_COMMIT_MESSAGE)
                    }
                }

                // Workaround for using none-bare repository
                scmActions.clean(remoteProjectFile)
                scmActions.checkout(remoteProjectFile, testBranch)
                if (this is BaseMultiModuleScmProjectTestCase) {
                    scmActions.clean(remoteSubmoduleProjectFile)
                    scmActions.checkout(remoteSubmoduleProjectFile, testBranch)
                }

                it.scmCompareCommits()
            }
    }

}
