package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir

interface MultiModuleScmProjectTestCase : ScmProjectTestCase<MultiModuleScmProjectGiven> {

    @Deprecated("add to context")
    var projectDir: ProjectDir
    @Deprecated("add to context")
    var submoduleProjectDirs: List<ProjectDir>

//    fun setProjectVersion(projectFile: ProjectFile<File>, version: Version)

}
