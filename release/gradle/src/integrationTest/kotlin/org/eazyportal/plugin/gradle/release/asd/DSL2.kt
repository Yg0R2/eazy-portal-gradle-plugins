package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
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
        fun initializeProject(tempDir: File)

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
        override fun initializeProject(@TempDir tempDir: File) {
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

    abstract class BaseScmProjectTestCase : TestCase<ScmProjectGiven<BaseScmProjectTestCase>, ScmProjectWhen, ScmProjectThen> {

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

    abstract class BaseSingleModuleScmProjectTestCase : BaseScmProjectTestCase() {
        override fun initializeGradleProjectBuilder(): GradleProjectBuilder =
            GradleProjectBuilder(projectDir.remote)
    }

    open class BaseSingleModuleGitFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseSingleModuleTrunkFlowScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseSingleModuleCustomizedScmProjectTestCase : BaseSingleModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    abstract class BaseMultiModuleScmProjectTestCase : BaseScmProjectTestCase() {

        protected lateinit var submoduleProjectDirs: List<ProjectDir>

        override fun initializeGradleProjectBuilder(): GradleProjectBuilder {
            TODO("Not yet implemented")
        }
    }

    open class BaseMultiModuleGitFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseMultiModuleTrunkFlowScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
        }
    }

    open class BaseMultiModuleCustomizedScmProjectTestCase : BaseMultiModuleScmProjectTestCase() {
        @BeforeEach
        override fun initializeProject(@TempDir tempDir: File) {
            println("[HERE] [initializeProject] ${this::class.java.simpleName}")
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
