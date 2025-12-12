package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.BaseGradleProjectTestCase

class GradleProjectGiven(
    private val testCase: BaseGradleProjectTestCase,
) : Given() {

    fun withGradleProject(initProjectBlock: GradleProjectBuilder.() -> Unit) {
        testCase.initializeGradleProjectBuilder()
            .apply { initProjectBlock() }
            .build()
    }

}
