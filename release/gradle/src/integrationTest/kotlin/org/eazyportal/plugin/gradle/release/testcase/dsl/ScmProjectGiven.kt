package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given

abstract class ScmProjectGiven<C : ScmProjectContext>(
    private val context: C,
    private val initializeProjectBlock: (C, (C) -> Unit) -> Unit,
) : Given<C>(context) {

    fun withScmProject(finalizeScmBlock: (C) -> Unit = {}) {
        initializeProjectBlock(context, finalizeScmBlock)
    }

}
