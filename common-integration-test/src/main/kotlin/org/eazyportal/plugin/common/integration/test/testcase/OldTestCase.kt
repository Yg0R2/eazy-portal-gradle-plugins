package org.eazyportal.plugin.common.integration.test.testcase

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder
import org.eazyportal.plugin.common.integration.test.testcase.builder.TestCaseBuilder
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given
import org.eazyportal.plugin.common.integration.test.testcase.dsl.TestScenario
import org.eazyportal.plugin.common.integration.test.testcase.dsl.Then
import org.eazyportal.plugin.common.integration.test.testcase.dsl.When

interface TestCase<G : Given, W : When, T : Then, SELF: TestCase<G, W, T, SELF>> {

    fun runTestCase(block: TestScenario<G, W, T, SELF>.() -> Unit)

}

interface OldTestCase<P : OldTestCase<P>> {

    fun givenTestCase(
        initProjectBlock: GradleProjectBuilder.() -> Unit = {},
    ): TestCaseBuilder.Given<P, *, *>

    fun initializeProject(
        gradleProjectBuilderBlock: GradleProjectBuilder.() -> Unit,
    )

}
