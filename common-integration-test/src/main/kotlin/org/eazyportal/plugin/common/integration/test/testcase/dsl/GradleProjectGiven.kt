package org.eazyportal.plugin.common.integration.test.testcase.dsl

import org.eazyportal.plugin.common.integration.test.gradle.GradleProjectBuilder

class GradleProjectGiven(
    private val context: GradleProjectContext,
    private val initializeProjectBlock: (GradleProjectContext, GradleProjectBuilder.() -> Unit) -> Unit,
) : Given<GradleProjectContext>(context) {

    fun withGradleProject(finalizeProjectBlock: GradleProjectBuilder.() -> Unit = {}) {
        initializeProjectBlock(context, finalizeProjectBlock)
    }

}
