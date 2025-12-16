package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given

abstract class ScmProjectGiven<C : ScmProjectContext>(
    private val context: C,
    private val initializeProjectBlock: (C, () -> Unit) -> Unit,
) : Given<C>(context) {

    fun withScmProject(finalizeScmBlock: () -> Unit = {}) {
        initializeProjectBlock(context, finalizeScmBlock)
    }

}
