package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig

interface ScmProjectTestCase<out C : ScmProjectTestContext> {

    val scmActions: TestScmActions<*>

    val scmConfig: ScmConfig

}
