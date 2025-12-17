package org.eazyportal.plugin.gradle.release.dsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.release.core.version.model.Version

interface SingleModuleScmProjectTestCase : ScmProjectTestCase<SingleModuleScmProjectTestContext>,
    TestCase<SingleModuleScmProjectGiven, ScmProjectWhen<SingleModuleScmProjectTestContext>, ScmProjectThen<SingleModuleScmProjectTestContext>> {

}
