package org.eazyportal.plugin.gradle.portal.project

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected lateinit var workingDir: File
    protected lateinit var projectDir: File

    @BeforeAll
    fun setUpBaseAcceptanceTest(
        @TempDir tempDir: File
    ) {
        workingDir = tempDir

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
    }

    protected fun File.copyIntoFromResources(
        fileName: String,
        resourceSubFolder: String = "",
    ): File {
        val resourceFile = "${this@BaseIntegrationTest::class.java.simpleName}/$resourceSubFolder/$fileName"
            .let(::getResourceFile)

        return resolve(fileName).also {
            it.parentFile.mkdirs()

            if (resourceFile.isDirectory) {
                resourceFile.copyRecursively(it, true)
            } else {
                it.writeText(resourceFile.readText())
            }
        }
    }

    protected fun createGradleRunner(
        projectFile: File,
        vararg arguments: String,
    ): GradleRunner =
        GradleRunner.create()
            .forwardOutput()
            .withArguments(
                "--stacktrace",
                "--warning-mode=all",
                *arguments
            ).withPluginClasspath()
            .withProjectDir(projectFile)

    protected fun File.initializeGradleProject(
        vararg subProjectNames: String,
    ) {
        createGradleRunner(this, "init", "--dsl", "kotlin")
            .build()

        copyIntoFromResources("build.gradle.kts")
        copyIntoFromResources("settings.gradle.kts")

        subProjectNames.forEach {
            Files.createDirectories(resolve(it).toPath())
        }
    }

    private fun getResourceFile(
        name: String,
    ): File =
        BaseIntegrationTest::class.java.classLoader.getResource(name)
            ?.let { File(it.toURI()) }
            ?: throw IllegalArgumentException("Resource is not found in classpath: $name")

    companion object {
        protected const val PROJECT_NAME = "dummy-project"
    }

}
