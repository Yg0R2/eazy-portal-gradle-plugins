package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir

interface SingleModuleScmProjectTestCase : ScmProjectTestCase<SingleModuleScmProjectGiven> {

    @Deprecated("add to context")
    var projectDir: ProjectDir

}
