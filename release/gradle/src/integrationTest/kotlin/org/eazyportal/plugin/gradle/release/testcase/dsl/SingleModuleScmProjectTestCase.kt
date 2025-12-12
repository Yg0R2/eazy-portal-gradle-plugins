package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir

interface SingleModuleScmProjectTestCase : ScmProjectTestCase {

    var projectDir: ProjectDir

}
