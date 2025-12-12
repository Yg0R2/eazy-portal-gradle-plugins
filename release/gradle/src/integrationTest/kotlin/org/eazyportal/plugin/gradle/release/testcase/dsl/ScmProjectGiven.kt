package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given
import org.eazyportal.plugin.gradle.release.testcase.BaseScmProjectTestCase

class ScmProjectGiven(
    private val testCase: BaseScmProjectTestCase,
) : Given() {

    fun withGradleProject(initProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        testCase.initializeGradleProjectBuilder()
            .apply { initProjectBlock() }
            .build()
    }

    fun withScmSetup(finalizeScmInitBlock: () -> Unit = {}) {
        testCase.initializeScm()

        finalizeScmInitBlock()
    }

}
