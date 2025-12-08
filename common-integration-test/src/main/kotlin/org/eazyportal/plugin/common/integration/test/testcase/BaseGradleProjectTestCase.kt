package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import java.io.File

open class BaseGradleProjectTestCase(
    protected open val workingDir: File,
) {

    val projectDir = workingDir.resolve(PROJECT_NAME)
        .also { it.mkdirs() }

    open fun initializeProject(
        gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit,
    ) {
        GradleProjectBuilder(projectDir)
            .apply { gradleProjectBuilderBlock() }
            .build()
    }

}
