package org.eazyportal.plugin.gradle

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

        GradleProjectBuilder(
            projectDir = remoteProjectDir,
            projectPluginIds = setOf("java", "org.eazyportal.plugin.gradle.release-gradle")
        ).build()

        scmUtils.initializeRepository(remoteProjectDir)
        scmUtils.clone(remoteProjectDir, projectDir)
    }

}
