package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

open class BaseProjectTestCase {

    protected lateinit var workingDir: File

    val projectDir: File
        get() = workingDir.resolve(PROJECT_NAME)
            .also { it.mkdirs() }

    @BeforeEach
    fun setUpWorkingDir(@TempDir tempDir: File) {
        workingDir = tempDir
    }

    open fun initializeProject(gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(projectDir)
            .apply { gradleProjectBuilderBlock() }
            .build()
    }

    protected open fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit = {},
    ): BaseProjectGiven<BaseProjectTestCase, BaseProjectWhen<BaseProjectTestCase>> =
        BaseProjectGiven(this, initProjectBlock)

}
