package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig

interface ScmProjectTestCase<G : ScmProjectGiven, T : ScmProjectThen> :
    TestCase<G, ScmProjectWhen, T> {

    val scmActions: TestScmActions<*>

    val scmConfig: ScmConfig

}
