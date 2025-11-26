package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.GradleTestFixtures
import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.eazyportal.plugin.release.core.scm.ScmConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class MultiModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

    val subModuleDir: File
        get() = workingDir.resolve("${ScmConstants.REMOTE}/${GradleTestFixtures.SUBMODULE_NAME}")
            .also { it.mkdirs() }

    @BeforeEach
    override fun setUpRepositories(@TempDir tempDir: File) {
        super.setUpRepositories(tempDir)

        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()
        scmUtils.initializeRepository(remoteProjectDir)

        GradleProjectBuilder(
            projectDir = subModuleDir,
            projectPluginIds = setOf("java")
        ).build()
        scmUtils.initializeRepository(subModuleDir)

        scmUtils.addSubmodule(remoteProjectDir, subModuleDir)
        scmUtils.commit(remoteProjectDir, "chore: add ${subModuleDir.name} submodule")

        scmUtils.clone(remoteProjectDir, projectDir)
    }

}
