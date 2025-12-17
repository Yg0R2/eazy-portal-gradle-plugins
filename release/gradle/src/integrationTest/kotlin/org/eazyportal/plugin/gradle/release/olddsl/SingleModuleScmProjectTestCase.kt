package org.eazyportal.plugin.gradle.release.olddsl

import org.eazyportal.plugin.common.integration.test.dsl.TestCase

interface SingleModuleScmProjectTestCase : ScmProjectTestCase<SingleModuleScmProjectTestContext>,
    TestCase<SingleModuleScmProjectGiven, ScmProjectWhen<SingleModuleScmProjectTestContext>, ScmProjectThen<SingleModuleScmProjectTestContext>> {

}
