package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.CommonTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.gradle.release.project.GradleProjectConstants.GRADLE_PROPERTIES_FILE_NAME
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.util.*

abstract class BaseIntegrationTest {

    protected val gitActions = GitActions(CommandLineExecutor())

    protected lateinit var originProjectDir: File
    protected lateinit var originProjectFile: ProjectFile<File>
    protected lateinit var projectFile: ProjectFile<File>
    protected lateinit var projectDir: File
    protected lateinit var workingDir: File

    protected val originProjectActions: ProjectActions<File>
        get() = GradleProjectActions(projectFile)
    protected val projectActions: ProjectActions<File>
        get() = GradleProjectActions(projectFile)

    @BeforeEach
    fun setUpBaseIntegrationTest(@TempDir tempDir: File) {
        workingDir = tempDir

        originProjectDir = workingDir.resolve("$REMOTE/$PROJECT_NAME")
            .also { it.mkdirs() }

        originProjectFile = FileSystemProjectFile(originProjectDir)

        projectDir = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

        projectFile = FileSystemProjectFile(projectDir)
    }

    protected fun ProjectFile<File>.createDummyComment(
        branch: String,
        commitMessage: String = DUMMY_COMMIT_MESSAGE,
    ) {
        gitActions.checkout(this, branch)

        createDummyFile()

        gitActions.add(this, DUMMY_FILE_NAME)
        gitActions.commit(this, commitMessage)
    }

    protected fun ProjectFile<File>.createDummyFile() {
        resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
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

            copyIntoFromResources(this@BaseIntegrationTest::class.java.simpleName, GRADLE_PROPERTIES_FILE_NAME)

            gitActions.add(this, "*")
            gitActions.commit(this, "chore: add gradle.properties")

            // TODO: maybe move it into `initializeGitProject`
            gitActions.execute(this, "branch", FEATURE_BRANCH)
        }
    }

    protected fun File.initializeGitProject(
        vararg subModuleNames: String,
    ) {
        copyIntoFromResources(resourcePath = "README.adoc")

        with(FileSystemProjectFile(this)) {
            gitActions.execute(this, "init", "--initial-branch=$RELEASE_BRANCH")
            gitActions.add(this, ".gitattributes", ".gitignore", "README.adoc")
            gitActions.commit(this, "initial commit")
        }
    }

    protected fun File.initializeGradleProject(
        vararg subProjectNames: String,
    ) {
        createGradleRunner(this, "init", "--dsl", "kotlin")
            .build()

        copyIntoFromResources(resourcePath = "build.gradle.kts")
        copyIntoFromResources(resourcePath = "settings.gradle.kts")

        subProjectNames.forEach {
            Files.createDirectories(resolve(it).toPath())
        }
    }

    companion object {
        protected const val DUMMY_FILE_NAME = "dummy.txt"
        protected const val DUMMY_COMMIT_MESSAGE = "chore: update $DUMMY_FILE_NAME"
    }

}
