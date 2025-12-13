package org.eazyportal.plugin.gradle.release.testcase.dsl

import org.eazyportal.plugin.gradle.release.testcase.dsl.model.ProjectDir

interface MultiModuleScmProjectTestCase : SingleModuleScmProjectTestCase, ScmProjectTestCase {

    var submoduleProjectDirs: List<ProjectDir>

//    fun setProjectVersion(projectFile: ProjectFile<File>, version: Version)

}
