package org.eazyportal.plugin.gradle.release.qwe

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.eazyportal.plugin.common.integration.test.gradle.GradleUtils.createGradleRunner
import org.eazyportal.plugin.common.integration.test.testcase.dsl.ExecutionResult
import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.dsl.TestContext
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.common.integration.test.dsl.then.ThenContext
import org.eazyportal.plugin.common.integration.test.dsl.`when`.When
import org.eazyportal.plugin.common.integration.test.dsl.`when`.WhenContext
import org.eazyportal.plugin.common.integration.test.dsl.annotation.IntegrationTestDsl
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

interface ScmTestContext : TestContext {
    val scmActions: TestScmActions<File>

    val scmConfig: ScmConfig

    val projectDir: ProjectDir
}

data class SingleModuleScmProjectContext(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val projectDir: ProjectDir,
) : ScmTestContext

data class MultiModuleScmProjectContext(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    override val projectDir: ProjectDir,
    val submoduleProjectDirs: List<ProjectDir>,
) : ScmTestContext


class ScmProjectGiven<C : ScmTestContext>(
    private val context: C,
    private val initializeProjectBlock: (C, C.() -> Unit) -> Unit,
) : Given<C>(context) {
    fun withScmProject(finalizeScmBlock: C.() -> Unit = {}) {
        initializeProjectBlock(context, finalizeScmBlock)
    }
}

class ScmProjectWhen<C : ScmTestContext>(
    private val context: C,
) : When<C>(context) {
    fun gradleTaskSucceeds(taskName: String, vararg args: String): BuildResult =
        createGradleRunner(context.projectDir.localDir, taskName, *args)
            .build()
}

class ScmProjectThen<C : ScmTestContext>(
    private val context: C,
    private val executionResult: ExecutionResult
) : Then<C>(context) {
    fun gradleTaskOutput(block: ListAssert<String>.() -> Unit) {
        block(
            assertThat(
                executionResult.actual<BuildResult>()
                    .output
                    .lines()
            )
        )
    }

    fun scmCommitsIn(
        projectFileProvider: C.() -> ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    ) {
        block(assertThat(context.scmActions.getCommits(projectFileProvider(context))))
    }
}


@IntegrationTestDsl
class TestScenario<G : Given<out GivenContext>, W : When<out WhenContext>, T : Then<out ThenContext>>(
    private val givenFactory: () -> G,
    private val whenFactory: () -> W,
    private val thenFactory: (ExecutionResult) -> T,
) {

    private val executionResult = ExecutionResult()

    fun givenTestCase(block: G.() -> Unit) {
        givenFactory().block()
    }

    fun whenExecute(block: W.() -> Any) {
        whenFactory()
            .block()
            .also(executionResult::set)
    }

    fun thenVerify(block: T.() -> Unit) {
        thenFactory(executionResult).block()
    }

}

@IntegrationTestDsl
interface TestCase<G : Given<GivenContext>, W : When<WhenContext>, T : Then<ThenContext>> {
    fun runTestCase(block: TestScenario<G, W, T>.() -> Unit)
}

interface ScmProjectTestCase<C : ScmTestContext> : TestCase<ScmProjectGiven<C>, ScmProjectWhen<C>, ScmProjectThen<C>>

abstract class BaseScmProjectTestCase<C : ScmTestContext> : ScmProjectTestCase<C> {

    final override fun runTestCase(block: TestScenario<ScmProjectGiven<C>, ScmProjectWhen<C>, ScmProjectThen<C>>.() -> Unit) {
        val context = createContext()
    }

    protected abstract fun createContext(): C
}


interface SingleModuleScmProjectTestCase : ScmProjectTestCase<SingleModuleScmProjectContext>

open class BaseSingleModuleScmProjectTestCase : SingleModuleScmProjectTestCase,
    BaseScmProjectTestCase<SingleModuleScmProjectContext>() {
    override fun createContext(): SingleModuleScmProjectContext {
        TODO("Not yet implemented")
    }
}


interface MultiModuleScmProjectTestCase : ScmProjectTestCase<MultiModuleScmProjectContext>

open class BaseMultiModuleScmProjectTestCase : MultiModuleScmProjectTestCase,
    BaseScmProjectTestCase<MultiModuleScmProjectContext>() {
    override fun createContext(): MultiModuleScmProjectContext {
        TODO("Not yet implemented")
    }
}

class DummyTest {

    interface DummyTestCase {
        fun `run test`()

        fun `run test 2`()
    }

    interface SingleModuleTestCase : DummyTestCase, ScmProjectTestCase<SingleModuleScmProjectContext> {

        override fun `run test`() = runTestCase {
            givenTestCase {
                withScmProject {
                    scmActions.add(projectDir.localProjectFile, ".")
                }

                withScenarioConfiguration {
                    scmActions.commit(projectDir.localProjectFile, "commit message")
                }
            }

            whenExecute {
                gradleTaskSucceeds("clean")
            }

            thenVerify {
                gradleTaskOutput {
                    contains("asd")
                }

                scmCommitsIn({ projectDir.localProjectFile }) {

                }
            }
        }

        @Test
        override fun `run test 2`() {
            TODO("Not yet implemented")
        }

    }

    interface MultiModuleTestCase : DummyTestCase, MultiModuleScmProjectTestCase {

        @Test
        override fun `run test 2`() {
            TODO("Not yet implemented")
        }

    }

    @Nested
    inner class SingleModuleGitFlowTestCase : SingleModuleTestCase, BaseSingleModuleScmProjectTestCase()

}