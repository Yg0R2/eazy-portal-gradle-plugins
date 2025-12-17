package org.eazyportal.plugin.gradle.portal.common.dsl

import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import java.io.File

class GradleProjectGiven(
    val workingDir: File,
) : Given {

    fun withGradleProject(finalizeProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        GradleProjectBuilder(workingDir)
            .apply(finalizeProjectBlock)
            .build()
    }

}
