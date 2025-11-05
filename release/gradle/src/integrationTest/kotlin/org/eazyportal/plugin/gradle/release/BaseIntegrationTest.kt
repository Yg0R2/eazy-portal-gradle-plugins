package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.gradle.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.gradle.release.core.project.ProjectActions
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.scm.GitActions
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.gradle.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected val gitActions = GitActions(CommandLineExecutor())

    protected lateinit var projectFile: ProjectFile<File>
    protected lateinit var projectDir: File
    protected lateinit var workingDir: File

    protected val projectActions: ProjectActions<File>
        get() = GradleProjectActions(projectFile)

    @BeforeAll
    fun setUpBaseIntegrationTest(
        @TempDir tempDir: File
    ) {
        workingDir = tempDir

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

        projectFile = FileSystemProjectFile(projectDir)
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
                "-Pversion=0.0.1-SNAPSHOT",
                "--no-configuration-cache",
                *arguments,
            ).withPluginClasspath()
//            .withGradleVersion("9.1.0")
            .withProjectDir(projectFile)

    protected fun File.initializeGitAndGradleProject(
        vararg subModuleNames: String,
    ) {
        initializeGradleProject(*subModuleNames)
        initializeGitProject(*subModuleNames)

        gitActions.add(projectFile, "*")
        gitActions.commit(projectFile, "initialize project")

        // TODO: maybe move it into `initializeGitProject`
        gitActions.execute(projectFile, "branch", FEATURE_BRANCH)
    }

    protected fun File.initializeGitProject(
        vararg subModuleNames: String,
    ) {
        projectDir.copyIntoFromResources("README.adoc")

        gitActions.execute(projectFile, "init", "--initial-branch=$MAIN_BRANCH", )
        gitActions.add(projectFile, ".gitattributes", ".gitignore", "README.adoc")
        gitActions.commit(projectFile, "initial commit")
    }

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
