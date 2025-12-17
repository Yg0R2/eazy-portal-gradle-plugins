package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase
import org.eazyportal.plugin.gradle.release.dsl.SingleModuleScmProjectGiven

interface SingleModuleScmProjectTestCase : ScmProjectTestCase<SingleModuleScmProjectTestContext>,
    TestCase<SingleModuleScmProjectGiven, ScmProjectWhen<SingleModuleScmProjectTestContext>, ScmProjectThen<SingleModuleScmProjectTestContext>> {

}
