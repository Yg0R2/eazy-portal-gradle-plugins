package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given

class ScmProjectGiven(
    private val gradleProjectBuilderFactory: () -> GradleProjectBuilder,
    private val scmSetUp: () -> Unit,
) : Given() {

    fun withGradleProject(initProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        gradleProjectBuilderFactory()
            .apply { initProjectBlock() }
            .build()
    }

    fun withScmSetUp(finalizeScmInitBlock: () -> Unit = {}) {
        scmSetUp()

        finalizeScmInitBlock()
    }

}
