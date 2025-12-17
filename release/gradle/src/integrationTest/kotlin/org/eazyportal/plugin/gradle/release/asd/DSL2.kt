package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.ProjectTestFixtures.SUBMODULE_NAMES
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestGitActions
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

object DSL2 {

    interface Given {
        fun withScenarioConfiguration(block: () -> Unit) {
            block()
        }
    }
    interface When
    interface Then

    class TestScenario<G : Given, W : When, T : Then>(
        private val givenFactory: () -> G,
        private val whenFactory: () -> W,
        private val thenFactory: () -> T,
    ) {
        fun givenScenario(block: G.() -> Unit) {
            givenFactory().block()
        }

        fun whenExecute(block: W.() -> Unit) {
            whenFactory().block()
        }

        fun thenValidate(block: T.() -> Unit) {
            thenFactory().block()
        }
    }

    interface TestCase<G : Given, W : When, T : Then> {
        fun initializeWorkingDir(tempDir: File)

        fun runTestCase(block: TestScenario<G, W, T>.() -> Unit)
    }

    // -------------------------------------------------------------
    // Basic implementation
    // -------------------------------------------------------------
    class SimpleProjectGiven(
        private val testCase: SimpleProjectTestCase,
    ) : Given {
        fun withGradleProject(initProjectBlock: GradleProjectBuilder.() -> Unit) {
            testCase.initializeGradleProjectBuilder()
                .apply { initProjectBlock() }
                .build()

            println("[HERE] [withGradleProject] init gradle")
        }
    }
    class SimpleProjectWhen : When
    class SimpleProjectThen : Then

    open class SimpleProjectTestCase : TestCase<SimpleProjectGiven, SimpleProjectWhen, SimpleProjectThen> {

        protected lateinit var workingDir: File

        @BeforeEach
        override fun initializeWorkingDir(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
    
            workingDir = tempDir
        }

        fun initializeGradleProjectBuilder(): GradleProjectBuilder =
            GradleProjectBuilder(workingDir)

        override fun runTestCase(
            block: TestScenario<SimpleProjectGiven, SimpleProjectWhen, SimpleProjectThen>.() -> Unit
        ) {
            println("[HERE] [runTestCase] ${this::class.java.simpleName}")

            TestScenario(
                givenFactory = { SimpleProjectGiven(this) },
                whenFactory = { SimpleProjectWhen() },
                thenFactory = { SimpleProjectThen() },
            ).block()
        }
    }

    // -------------------------------------------------------------
    // SCM implementation
    // -------------------------------------------------------------
    class ScmProjectGiven<P : BaseScmProjectTestCase>(
        private val testCase: P,
    ) : Given {
        fun withGradleProject(initProjectBlock: GradleProjectBuilder.() -> Unit) {
            testCase.initializeGradleProjectBuilder()
                .apply { initProjectBlock() }
                .build()
            println("[HERE] [withGradleProject] init gradle")
        }
    }
    class ScmProjectWhen : When
    class ScmProjectThen : Then

    data class ProjectDir(
        val local: File,
        val remote : File,
    )

    abstract class BaseScmProjectTestCase(
        protected open val scmActions: TestScmActions<File>,
        protected open val scmConfig: ScmConfig,
    ) : TestCase<ScmProjectGiven<BaseScmProjectTestCase>, ScmProjectWhen, ScmProjectThen> {

        protected lateinit var workingDir: File
        protected lateinit var projectDir: ProjectDir

        abstract fun initializeGradleProjectBuilder(): GradleProjectBuilder

        override fun runTestCase(block: TestScenario<ScmProjectGiven<BaseScmProjectTestCase>, ScmProjectWhen, ScmProjectThen>.() -> Unit) {
            println("[HERE] [runTestCase] ${this::class.java.simpleName}")

            TestScenario(
                givenFactory = { ScmProjectGiven(this) },
                whenFactory = { ScmProjectWhen() },
                thenFactory = { ScmProjectThen() },
            ).block()
        }
    }

    abstract class BaseSingleModuleScmProjectTestCase(
        override val scmActions: TestScmActions<File>,
        override val scmConfig: ScmConfig,
    ) : BaseScmProjectTestCase(scmActions, scmConfig) {

        @BeforeEach
        final override fun initializeWorkingDir(@TempDir tempDir: File) {
            workingDir = tempDir

            projectDir = ProjectDir(
                local = workingDir.resolve(PROJECT_NAME),
                remote = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME"),
            )

            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseSingleModuleGitFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    open class BaseSingleModuleTrunkFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    open class BaseSingleModuleCustomizedScmProjectTestCase : BaseSingleModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig(
            featureBranch = "dummy-feature-branch",
            releaseBranch = "dummy-release-branch",
            remote = "upstream"
        ),
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    abstract class BaseMultiModuleScmProjectTestCase(
        override val scmActions: TestScmActions<File>,
        override val scmConfig: ScmConfig,
    ) : BaseScmProjectTestCase(scmActions, scmConfig) {

        protected lateinit var submoduleProjectDirs: List<ProjectDir>

        @BeforeEach
        final override fun initializeWorkingDir(@TempDir tempDir: File) {
            workingDir = tempDir

            projectDir = ProjectDir(
                local = workingDir.resolve(PROJECT_NAME),
                remote = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME"),
            )

            submoduleProjectDirs = SUBMODULE_NAMES.map {
                ProjectDir(
                    local = projectDir.local.resolve(it),
                    remote = projectDir.remote.resolve(it),
                )
            }

            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseMultiModuleGitFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.GIT_FLOW,
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    open class BaseMultiModuleTrunkFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig.TRUNK_BASED_FLOW,
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    open class BaseMultiModuleCustomizedScmProjectTestCase : BaseMultiModuleScmProjectTestCase(
        TestGitActions(CommandLineExecutor()),
        ScmConfig(
            featureBranch = "dummy-feature-branch",
            releaseBranch = "dummy-release-branch",
            remote = "upstream"
        ),
    ) {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

}

class DSL2Test {

    interface DSL2TestCase {

        fun `test execution`()

    }

    @Nested
    inner class SimpleDSL2ProjectTest : DSL2.SimpleProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                withGradleProject {

                }
                println("[HERE] [test - given] ${this::class.java.simpleName}")

                withScenarioConfiguration {
                    println("[HERE] [test - withConfiguration] ${this::class.java.simpleName}")
                }
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleGitFlowDSL2ProjectTest : DSL2.BaseSingleModuleGitFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                withGradleProject {

                }
                println("[HERE] [test - given] ${this::class.java.simpleName}")

                withScenarioConfiguration {
                    println("[HERE] [test - withConfiguration] ${this::class.java.simpleName}")
                }
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleTrunkFlowDSL2ProjectTest : DSL2.BaseSingleModuleTrunkFlowScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class SingleModuleCustomizedDSL2ProjectTest : DSL2.BaseSingleModuleCustomizedScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleGitFlowDSL2ProjectTest : DSL2.BaseMultiModuleGitFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleTrunkFlowDSL2ProjectTest : DSL2.BaseMultiModuleTrunkFlowScmProjectTestCase(), DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

    @Nested
    inner class MultiModuleCustomizedDSL2ProjectTest : DSL2.BaseMultiModuleCustomizedScmProjectTestCase(),
        DSL2TestCase {

        @Test
        override fun `test execution`() = runTestCase {
            givenScenario {
                println("[HERE] [test - given] ${this::class.java.simpleName}")
            }
            whenExecute {
                println("[HERE] [test - when] ${this::class.java.simpleName}")
            }
            thenValidate {
                println("[HERE] [test - then] ${this::class.java.simpleName}")
            }
        }

    }

}
