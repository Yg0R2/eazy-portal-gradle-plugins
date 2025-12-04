package org.eazyportal.plugin.gradle.release

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.GradleUtils.createGradleRunner
import org.eazyportal.plugin.gradle.release.asd.BaseMultiModuleScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseScmProjectTestCase
import org.eazyportal.plugin.gradle.release.asd.BaseSingleModuleScmProjectTestCase
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

        @Deprecated("")
        fun thenAssert(assertBlock: T.(Then<T>) -> Unit) {
            testCase.assertBlock(Then(testCase, result))
        }

        fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): Then<T> =
            Then(testCase, result)
                .also { it.thenAssertTaskOutput(block) }

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

        @Deprecated("")
        fun scmCommits(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.getCommits(projectFile)))
        }

        @Deprecated("")
        fun scmCompareCommits(
            block: ListAssert<String>.() -> Unit = {}
        ) {
            scmCompareCommits(testCase.projectFile, testCase.remoteProjectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCompareCommits(testCase.submoduleProjectFile, testCase.remoteSubmoduleProjectFile, block)
            }
        }

        @Deprecated("")
        fun scmCompareCommits(
            projectFile: ProjectFile<File>,
            remoteProjectFile: ProjectFile<File>,
            alsoAssertBlock: ListAssert<String>.() -> Unit = {},
        ) {
            assertThat(testCase.scmActions.getCommits(projectFile))
                .containsExactlyElementsOf(testCase.scmActions.getCommits(remoteProjectFile))
                .alsoAssertBlock()
        }

        @Deprecated("")
        fun scmLocalCommits(block: ListAssert<String>.() -> Unit) {
            scmCommits(testCase.projectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCommits(testCase.submoduleProjectFile, block)
            }
        }

        @Deprecated("")
        fun scmRemoteCommits(block: ListAssert<String>.() -> Unit) {
            scmCommits(testCase.remoteProjectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                scmCommits(testCase.remoteSubmoduleProjectFile, block)
            }
        }

        @Deprecated("")
        fun scmLocalStatus(block: ListAssert<String>.() -> Unit) {
            scmStatus(testCase.projectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                block(assertThat(testCase.scmActions.status(testCase.submoduleProjectFile)))
            }
        }

        @Deprecated("")
        fun scmRemoteStatus(block: ListAssert<String>.() -> Unit) {
            scmStatus(testCase.remoteProjectFile, block)

            if (testCase is BaseMultiModuleScmProjectTestCase) {
                block(assertThat(testCase.scmActions.status(testCase.remoteSubmoduleProjectFile)))
            }
        }

        @Deprecated("")
        fun scmStatus(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.status(projectFile)))
        }

        @Deprecated("")
        fun taskOutput(block: ListAssert<String>.() -> Unit) {
            block(
                assertThat(buildResult.output.lines()),
            )
        }

        // Project asserts

        fun thenAssertProjectVersion(
            expectedVersion: Version,
        ): Then<T> =
            apply {
                assertThat(testCase.getProjectVersion(testCase.projectFile))
                    .isEqualTo(expectedVersion)

                if (testCase is BaseMultiModuleScmProjectTestCase) {
                    assertThat(testCase.getProjectVersion(testCase.submoduleProjectFile))
                        .isEqualTo(expectedVersion)
                }
            }

        // SCM asserts

        fun thenAssertScm(block: T.(ScmAssertion<T>) -> Unit): Then<T> =
            apply {
                testCase.block(ScmAssertion(testCase))
            }

        fun thenAssertScmIfMultiModule(block: BaseMultiModuleScmProjectTestCase.(ScmAssertion<T>) -> Unit): Then<T> =
            apply {
                (testCase as BaseMultiModuleScmProjectTestCase)
                    .block(ScmAssertion(testCase))
            }

        fun thenAssertScmIfSingleModule(block: BaseSingleModuleScmProjectTestCase.(ScmAssertion<T>) -> Unit): Then<T> =
            apply {
                (testCase as BaseSingleModuleScmProjectTestCase)
                    .block(ScmAssertion(testCase))
            }

        // Task output asserts

        fun thenAssertTaskOutput(block: ListAssert<String>.() -> Unit): Then<T> =
            apply {
                block(assertThat(buildResult.output.lines()))
            }

    }

    class ScmAssertion<T : BaseScmProjectTestCase>(
        private val testCase: T,
    ) {

        fun commitsIn(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.getCommits(projectFile)))
        }

        fun compareCommitsIn(
            projectFile: ProjectFile<File>,
            remoteProjectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit = {},
        ) {
            assertThat(testCase.scmActions.getCommits(projectFile))
                .containsExactlyElementsOf(testCase.scmActions.getCommits(remoteProjectFile))
                .block()
        }

        fun statusCleanIn(
            projectFile: ProjectFile<File>,
            branch: String,
        ) {
            statusIn(projectFile) {
                containsExactly(
                    "On branch $branch",
                    "Your branch is up to date with '${testCase.scmConfig.remote}/$branch'.",
                    "nothing to commit, working tree clean",
                )
            }
        }

        fun statusIn(
            projectFile: ProjectFile<File>,
            block: ListAssert<String>.() -> Unit,
        ) {
            block(assertThat(testCase.scmActions.status(projectFile)))
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