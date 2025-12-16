package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.dsl.given.GivenContext
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import java.io.File

class GradleProjectGivenContext(
    private val projectDir: File,
) : GivenContext {

    fun withGradleProject(block: GradleProjectBuilder.() -> Unit) {
        GradleProjectBuilder(projectDir)
            .block()
    }

}
