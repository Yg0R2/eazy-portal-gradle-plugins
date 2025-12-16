package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.dsl.Given

class ScmProjectGiven(
    private val context: ScmProjectContext,
    private val initializeProjectBlock: () -> Unit,
) : Given<ScmProjectContext>(context) {

    fun withScmProject(finalizeScmBlock: () -> Unit = {}) {
        initializeProjectBlock()

        finalizeScmBlock()
    }

}
