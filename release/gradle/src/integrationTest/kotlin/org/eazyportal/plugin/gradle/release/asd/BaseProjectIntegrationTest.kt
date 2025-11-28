package org.eazyportal.plugin.gradle.release.asd

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.GradleTestFixtures.SUBMODULE_NAME
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.ScmActions
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseProjectIntegrationTest {

    lateinit var workingDir: File

    abstract fun initializeProject()

    protected abstract fun configureProject()

}

abstract class BaseScmProjectIntegrationTest(
    open val scmConfig: ScmConfig,
    open val scmActions: TestScmActions<File>
) : BaseProjectIntegrationTest() {

    val projectFile: ProjectFile<File>
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

    val remoteProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$PROJECT_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

}

abstract class BaseSingleModuleScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseScmProjectIntegrationTest(scmConfig, scmActions) {

}

abstract class BaseSingleModuleGitFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectIntegrationTest(scmConfig, scmActions) {

    override fun initializeProject() {
        GradleProjectBuilder(
            projectDir = remoteProjectFile.getFile(),
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmActions.initializeRepository(remoteProjectFile)

        // Create remote feature branch
        scmActions.execute(remoteProjectFile, "branch", scmConfig.featureBranch)

        scmActions.clone(remoteProjectFile, projectFile)

        configureProject()
    }

}

abstract class BaseSingleModuleTrunkFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectIntegrationTest(scmConfig, scmActions) {

}

abstract class BaseSingleModuleCustomFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseSingleModuleScmProjectIntegrationTest(scmConfig, scmActions) {

}

abstract class BaseMultiModuleScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseScmProjectIntegrationTest(scmConfig, scmActions) {

    protected val submoduleProjectFile: ProjectFile<File>
        get() = projectFile.resolve(SUBMODULE_NAME)
            .also { it.getFile().mkdirs() }

    protected val remoteSubmoduleProjectFile: ProjectFile<File>
        get() = workingDir.resolve("${scmConfig.remote}/$SUBMODULE_NAME")
            .also { it.mkdirs() }
            .let { FileSystemProjectFile(it) }

}

abstract class BaseMultiModuleGitFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseMultiModuleScmProjectIntegrationTest(scmConfig, scmActions) {

}

abstract class BaseMultiModuleTrunkFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseMultiModuleScmProjectIntegrationTest(scmConfig, scmActions) {

}

abstract class BaseMultiModuleCustomFlowScmProjectIntegrationTest(
    override val scmConfig: ScmConfig,
    override val scmActions: TestScmActions<File>,
) : BaseMultiModuleScmProjectIntegrationTest(scmConfig, scmActions) {

}
