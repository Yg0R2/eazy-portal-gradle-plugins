package org.eazyportal.plugin.release.core.scm

import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.version.model.Version

interface ScmActions<T : Any> {

    fun add(projectFile: ProjectFile<T>, vararg filePaths: String)

    fun checkout(projectFile: ProjectFile<T>, toRef: String)

    fun clean(projectFile: ProjectFile<T>)

    fun commit(projectFile: ProjectFile<T>, message: String)

    fun fetch(projectFile: ProjectFile<T>, remote: String, vararg branches: String)

    fun getCommits(projectFile: ProjectFile<T>, fromRef: String? = null, toRef: String = "HEAD"): List<String>

    fun getCurrentBranch(projectFile: ProjectFile<T>): String

    fun getLastTag(projectFile: ProjectFile<T>, fromRef: String = "HEAD"): String

    fun getSubmodules(projectFile: ProjectFile<T>): List<String>

    fun getTags(projectFile: ProjectFile<T>, fromRef: String = "HEAD"): List<String>

    fun mergeNoCommit(projectFile: ProjectFile<T>, fromBranch: String)

    fun push(projectFile: ProjectFile<T>, remote: String, vararg branches: String)

//    fun status(projectFile: ProjectFile<T>): List<String>
//
    fun tag(projectFile: ProjectFile<T>, version: Version)

}
