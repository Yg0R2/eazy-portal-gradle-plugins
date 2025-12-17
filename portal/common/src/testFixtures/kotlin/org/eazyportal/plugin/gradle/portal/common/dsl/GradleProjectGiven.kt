package org.eazyportal.plugin.gradle.portal.common.dsl

import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import java.io.File

class GradleProjectGiven(
    val workingDir: File,
    private val setUpProjectBlock: (File, GradleProjectBuilder.() -> Unit) -> Unit,
) : Given {

    fun withGradleProject(finalizeProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        setUpProjectBlock(workingDir, finalizeProjectBlock)
    }

}
