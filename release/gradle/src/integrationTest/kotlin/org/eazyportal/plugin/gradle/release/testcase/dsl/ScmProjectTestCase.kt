package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.common.integration.test.testcase.TestCase
import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import java.io.File

interface ScmProjectTestCase : TestCase<ScmProjectGiven, ScmProjectWhen, ScmProjectThen> {

    var projectDir: ProjectDir

    val scmActions: TestScmActions<File>
    val scmConfig: ScmConfig

}
