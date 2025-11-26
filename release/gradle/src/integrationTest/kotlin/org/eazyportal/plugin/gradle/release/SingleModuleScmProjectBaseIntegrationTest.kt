package org.eazyportal.plugin.gradle.release

import org.eazyportal.plugin.common.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.scm.ScmUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class SingleModuleScmProjectBaseIntegrationTest(
    override val scmUtils: ScmUtils,
) : ScmProjectBaseIntegrationTest(scmUtils) {

    @BeforeEach
    override fun setUpRepositories(@TempDir tempDir: File) {
        super.setUpRepositories(tempDir)

        setupRemoteBeforeClone()

        scmUtils.clone(remoteProjectDir, projectDir)
    }

    protected open fun setupRemoteBeforeClone() {
        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
    }

}
