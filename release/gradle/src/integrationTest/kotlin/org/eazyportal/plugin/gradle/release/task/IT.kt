package org.eazyportal.plugin.gradle.release.task

import org.assertj.core.api.Assertions.assertThat
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.io.File


abstract class BaseSingleModuleIT {

    lateinit var projectFile: ProjectFile<File>

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        projectFile = FileSystemProjectFile(tempDir)
    }

}

abstract class BaseMultiModuleIT {

    lateinit var projectFile: ProjectFile<File>
    lateinit var submoduleProjectFile: ProjectFile<File>

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        projectFile = FileSystemProjectFile(tempDir.resolve("project")).also { it.getFile().mkdirs() }
        submoduleProjectFile = projectFile.resolve("submodule").also { it.getFile().mkdirs() }
    }

}

interface SetReleaseVersionTaskTestCase {

    fun `test 'run' set release`()

}


class SetReleaseVersionTaskIT {

    @Nested
    inner class SetReleaseVersionSingleModuleTestCase : BaseSingleModuleIT(), SetReleaseVersionTaskTestCase {

        @Test
        override fun `test 'run' set release`() {
            println(projectFile.toString())
        }

    }

    @Nested
    inner class SetReleaseVersionMultiModuleTestCase : BaseMultiModuleIT(), SetReleaseVersionTaskTestCase {

        @Test
        override fun `test 'run' set release`() {
            println(projectFile.toString())
            println(submoduleProjectFile.toString())
        }

    }

}

interface FinalizeReleaseVersionTaskTestCase {

    fun `test 'run' finalize release`()

}

class FinalizeReleaseVersionTaskIT {

    @Nested
    inner class FinalizeReleaseVersionSingleModuleTestCase : BaseSingleModuleIT(), FinalizeReleaseVersionTaskTestCase {

        @Test
        override fun `test 'run' finalize release`() {
            println(projectFile.toString())
        }

    }

    @Nested
    inner class FinalizeReleaseVersionMultiModuleTestCase : BaseMultiModuleIT(), FinalizeReleaseVersionTaskTestCase {

        @Test
        override fun `test 'run' finalize release`() {
            println(projectFile.toString())
            println(submoduleProjectFile.toString())
        }

    }

}

// ------- ENFORCE ----

class IntegrationTestCompletenessCheck {

    @Test
    fun `all IT classes must implement all required base fixtures`() {
        val classes = Reflections(
            ConfigurationBuilder()
                .forPackage(this::class.java.packageName)
                .setScanners(Scanners.SubTypes.filterResultsBy { true })
        ).getSubTypesOf(Any::class.java)

        val baseModuleITClasses = classes.filter {
            it.simpleName.matches("^Base.*ModuleIT$".toRegex())
        }

        val itClasses = classes.filter {
            it.simpleName.matches(".*TaskIT$".toRegex())
        }

        val errors= itClasses.flatMap { itClass ->
            val nested = itClass.declaredClasses.toSet()

            baseModuleITClasses.filter { baseModuleITClass ->
                nested.none { baseModuleITClass.isAssignableFrom(it) }
            }.map { "${itClass.simpleName} is missing nested test case extending ${it.simpleName}." }
        }.filter { it.isNotEmpty() }

        assertThat(errors.isEmpty())
            .withFailMessage {
                errors.joinToString(
                    System.lineSeparator(),
                    "${System.lineSeparator()}========== Missing Task Integration Tests ==========${System.lineSeparator()}",
                    "${System.lineSeparator()}====================================================",
                ) { "  - $it" }
            }.isTrue
    }
}
