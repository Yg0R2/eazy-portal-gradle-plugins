package org.eazyportal.plugin.gradle.release.dsl

import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert
import org.eazyportal.plugin.common.integration.test.dsl.then.Then
import org.eazyportal.plugin.gradle.release.dsl.model.ProjectDir
import org.eazyportal.plugin.release.core.TestScmActions
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.model.ScmConfig
import org.eazyportal.plugin.release.core.version.model.Version
import java.io.File

interface ScmProjectThen : Then {

    val scmActions: TestScmActions<File>
    val scmConfig: ScmConfig

    //------------------------------------
    // Gradle
    //------------------------------------

    fun gradleTaskOutput(block: ListAssert<String>.() -> Unit)

    //------------------------------------
    // Project
    //------------------------------------

    fun projectVersionIn(
        projectFile: ProjectFile<File>,
        block: ObjectAssert<Version>.() -> Unit,
    )

    //------------------------------------
    // SCM Commit
    //------------------------------------

    fun scmCommitsIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    )

    fun scmCompareCommitsIn(
        left: ProjectFile<File>,
        right: ProjectFile<File>,
        alsoAssertBlock: ListAssert<String>.() -> Unit = {}
    )

    //------------------------------------
    // SCM Status
    //------------------------------------

    fun scmClenStatusIn(
        projectFile: ProjectFile<File>,
        branch: String,
    )

    fun scmStatusIn(
        projectFile: ProjectFile<File>,
        block: ListAssert<String>.() -> Unit,
    )

}
