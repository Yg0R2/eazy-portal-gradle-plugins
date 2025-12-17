package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig

interface ScmProjectTestCase :
    TestCase<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {

    val scmActions: TestScmActions<*>

    val scmConfig: ScmConfig

}
