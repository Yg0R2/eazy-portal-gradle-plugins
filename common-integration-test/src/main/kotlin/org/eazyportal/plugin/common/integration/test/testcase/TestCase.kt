package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder

interface TestCase<P : TestCase<P>> {

    fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit = {},
    ): TestCaseBuilder.Given<P, *>

    fun initializeProject(
        gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit,
    )

}
