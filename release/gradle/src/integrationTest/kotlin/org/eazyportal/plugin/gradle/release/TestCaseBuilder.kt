package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.version.model.Version
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.reflect.KClass

object TestCaseBuilder {

    class Given<T : BaseScmProjectTestCase>(
        private val testCase: T,
        configureProjectBlock: T.() -> Unit,
    ) {

        init {
            testCase.initializeProject()

            configureProjectBlock(testCase)
        }

        fun whenGradleTask(
            taskName: String,
            vararg arguments: String,
            gradleTaskBlock: T.(GradleRunner) -> BuildResult,
        ): When<T> =
            When(
                testCase,
                testCase.gradleTaskBlock(createGradleRunner(testCase.projectFile.getFile(), taskName, *arguments))
            )

        fun whenGradleTaskFails(
            taskName: String,
            vararg arguments: String,
        ): When<T> =
            createGradleRunner(testCase.projectFile.getFile(), taskName, *arguments)
                .buildAndFail()
                .let { When(testCase, it) }

        fun whenGradleTaskSucceeds(
            taskName: String,
            vararg arguments: String,
        ): When<T> =
            createGradleRunner(testCase.projectFile.getFile(), taskName, *arguments)
                .build()
                .let { When(testCase, it) }

    }

    class When<T : BaseScmProjectTestCase>(
        private val testCase: T,
        private val result: BuildResult,
    ) {

        fun thenAssert(assertBlock: T.(Then<T>) -> Unit) {
            testCase.assertBlock(Then(testCase, result))
        }

    }

    class Then<T : BaseScmProjectTestCase>(
        private val testCase: T,
        private val buildResult: BuildResult,
    ) {

        fun projectVersion(
            versionBlock: ObjectAssert<Version>.() -> Unit,
        ) {
            versionBlock(assertThat(testCase.getProjectVersion(testCase.projectFile)))

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                versionBlock(assertThat(testCase.getProjectVersion(testCase.submoduleProjectFile)))
            }
        }

        fun scmCommits(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.getCommits(projectFile)))
        }

        fun scmCompareCommits() {
            scmCompareCommits(testCase.projectFile, testCase.remoteProjectFile)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCompareCommits(testCase.submoduleProjectFile, testCase.remoteSubmoduleProjectFile)
            }
        }

        fun scmCompareCommits(
            projectFile: ProjectFile<File>,
            remoteProjectFile: ProjectFile<File>,
        ) {
            assertThat(testCase.scmActions.getCommits(projectFile))
                .containsExactlyElementsOf(testCase.scmActions.getCommits(remoteProjectFile))
        }

        fun scmLocalCommits(block: ListAssert<String>.() -> Unit) {
            scmCommits(testCase.projectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCommits(testCase.submoduleProjectFile, block)
            }
        }

        fun scmRemoteCommits(block: ListAssert<String>.() -> Unit) {
            scmCommits(testCase.remoteProjectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCommits(testCase.remoteSubmoduleProjectFile, block)
            }
        }

        fun scmLocalStatus(block: ListAssert<String>.() -> Unit) {
            scmStatus(testCase.projectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                block(assertThat(testCase.scmActions.status(testCase.submoduleProjectFile)))
            }
        }

        fun scmRemoteStatus(block: ListAssert<String>.() -> Unit) {
            scmStatus(testCase.remoteProjectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                block(assertThat(testCase.scmActions.status(testCase.remoteSubmoduleProjectFile)))
            }
        }

        fun scmStatus(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.status(projectFile)))
        }

        fun taskOutput(block: ListAssert<String>.() -> Unit) {
            block(
                assertThat(buildResult.output.lines()),
            )
        }

    }

    fun <T : BaseScmProjectTestCase> givenTestCase(
        clazz: KClass<out T>,
        workingDir: File,
        configureProjectBlock: T.() -> Unit = {},
    ): Given<out T> =
        Given(
            clazz.java.getDeclaredConstructor(File::class.java).newInstance(workingDir),
            configureProjectBlock,
        )

}