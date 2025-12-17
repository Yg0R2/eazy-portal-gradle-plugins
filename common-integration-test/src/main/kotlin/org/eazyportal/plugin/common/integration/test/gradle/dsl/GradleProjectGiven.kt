package org.eazyportal.plugin.common.integration.test.gradle.dsl

import org.eazyportal.plugin.common.integration.test.dsl.given.Given
import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder

class GradleProjectGiven(
    private val context: GradleProjectTestContext,
    private val setUpProjectBlock: (GradleProjectTestContext, GradleProjectBuilder.() -> Unit) -> Unit,
) : Given<GradleProjectTestContext>(context) {

    fun withGradleProject(finalizeProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        setUpProjectBlock(context, finalizeProjectBlock)
    }

}
