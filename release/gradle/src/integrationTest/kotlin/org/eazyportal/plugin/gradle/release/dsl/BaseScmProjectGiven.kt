package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

abstract class BaseScmProjectGiven(
    override val scmActions: TestScmActions<File>,
    override val scmConfig: ScmConfig,
    private val initScmProjectBlock: () -> Unit,
) : ScmProjectGiven {

    override fun withScmProject() {
        initScmProjectBlock()
    }

}
