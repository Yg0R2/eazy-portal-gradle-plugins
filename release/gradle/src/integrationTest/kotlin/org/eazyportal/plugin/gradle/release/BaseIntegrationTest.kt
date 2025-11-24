package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.ResourceUtils.copyIntoFromResources
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.GitUtils
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.gradle.release.project.GradleProjectActions
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions
import org.eazyportal.plugin.release.core.scm.ScmConstants.FEATURE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.RELEASE_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

abstract class BaseIntegrationTest {

    protected val gitActions = GitActions(CommandLineExecutor())
    protected val scmUtils: ScmUtils = GitUtils

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

    protected fun File.initializeGitAndGradleProject(
        vararg subModuleNames: String,
    ) {
        GradleProjectBuilder(
            projectDir = this,
            subProjectNames = subModuleNames.toSet(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()
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
        copyIntoFromResources(resourcePath = "README.adoc")

        with(FileSystemProjectFile(this)) {
            gitActions.execute(this, "init", "--initial-branch=$RELEASE_BRANCH")
            gitActions.add(this, ".gitattributes", ".gitignore", "README.adoc")
            gitActions.commit(this, "initial commit")
        }
    }

}
