package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected val gitActions = GitActions(CommandLineExecutor())

    protected lateinit var originProjectDir: File
    protected lateinit var originProjectFile: ProjectFile<File>
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

        originProjectDir = workingDir.resolve("$REMOTE/$PROJECT_NAME")
            .also { it.mkdirs() }

        originProjectFile = FileSystemProjectFile(originProjectDir)

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

        projectFile = FileSystemProjectFile(projectDir)
    }

    protected fun File.copyIntoFromResources(
        fileName: String,
        resourceSubFolder: String = "",
    ): File {
        val resourceFile = getResourceFile(fileName, resourceSubFolder)

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

        with(FileSystemProjectFile(this)) {
            gitActions.add(this, "*")
            gitActions.commit(this, "initialize project")

            // TODO: maybe move it into `initializeGitProject`
            gitActions.execute(this, "branch", FEATURE_BRANCH)
        }
    }

    protected fun File.initializeGitProject(
        vararg subModuleNames: String,
    ) {
        copyIntoFromResources("README.adoc")

        with(FileSystemProjectFile(this)) {
            gitActions.execute(this, "init", "--initial-branch=$MAIN_BRANCH")
            gitActions.add(this, ".gitattributes", ".gitignore", "README.adoc")
            gitActions.commit(this, "initial commit")
        }
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
        fileName: String,
        resourceSubFolder: String = "",
    ): File =
        with(this@BaseIntegrationTest::class.java) {
            val resource = classLoader.getResource(
                "${this@BaseIntegrationTest::class.java.simpleName}/$resourceSubFolder/$fileName"
            ) ?: classLoader.getResource("_common/$resourceSubFolder/$fileName")

            resource?.let { File(it.toURI()) }
                ?: throw IllegalArgumentException("Resource is not found in classpath: $name")
        }

    companion object {
        protected const val PROJECT_NAME = "dummy-project"
    }

}
