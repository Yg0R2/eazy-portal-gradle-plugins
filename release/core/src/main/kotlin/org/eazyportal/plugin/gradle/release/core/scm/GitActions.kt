package org.eazyportal.plugin.gradle.release.core.scm

import org.eazyportal.plugin.gradle.release.core.executor.CommandExecutor
import org.eazyportal.plugin.gradle.release.core.project.ProjectFile
import org.eazyportal.plugin.gradle.release.core.scm.exception.ScmActionException
import org.eazyportal.plugin.gradle.release.core.utils.isWindows
import org.eazyportal.plugin.gradle.release.core.version.model.Version

class GitActions<T: Any>(
    private val commandExecutor: CommandExecutor<ProjectFile<T>>
) : ScmActions<T> {

    override fun add(projectFile: ProjectFile<T>, vararg filePaths: String) {
        execute(projectFile, "add", *filePaths)
    }

    override fun checkout(projectFile: ProjectFile<T>, toRef: String) {
        execute(projectFile, "checkout", toRef)
    }

    override fun commit(projectFile: ProjectFile<T>, message: String) {
        execute(projectFile, "commit", "-m", message)
    }

    fun execute(projectFile: ProjectFile<T>, vararg gitCommands: String): String =
        runCatching {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, *gitCommands)
        }.getOrElse {
            throw ScmActionException(it)
        }

    override fun fetch(projectFile: ProjectFile<T>, remote: String) {
        execute(projectFile, "fetch", remote, "--tags", "--prune", "--prune-tags", "--recurse-submodules")

        val currentBranchName = execute(projectFile, "rev-parse", "--abbrev-ref", "HEAD")
        execute(projectFile, "reset", "--hard", "$remote/$currentBranchName")
    }

    override fun getCommits(projectFile: ProjectFile<T>, fromRef: String?, toRef: String): List<String> =
        listOfNotNull(fromRef, toRef)
            .joinToString("..")
            .let { execute(projectFile, "log", "--pretty=format:%s", it) }
            .split(LINE_BREAK_REGEX)
            .filter { it.isNotBlank() }

    override fun getLastTag(projectFile: ProjectFile<T>, fromRef: String): String =
        execute(projectFile, "describe", "--abbrev=0", "--tags", fromRef)
            .trim()

    override fun getSubmodules(projectFile: ProjectFile<T>): List<String> =
        execute(projectFile, "submodule")
            .lines()
            .asSequence()
            .map { it.replace(Regex("""^[\s\W]?\w+\s(.*?)\s?(\(.*\))?${'$'}"""), "$1") }
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .toList()

    override fun getTags(projectFile: ProjectFile<T>, fromRef: String): List<String> =
        execute(projectFile, "tag", "--sort=-creatordate", "--contains", fromRef)
            .split(LINE_BREAK_REGEX)
            .filter { it.isNotBlank() }

    override fun mergeNoCommit(projectFile: ProjectFile<T>, fromBranch: String) {
        execute(projectFile, "merge", "--no-ff", "--no-commit", "--strategy-option=theirs", fromBranch)
    }

    override fun push(projectFile: ProjectFile<T>, remote: String, vararg branches: String) {
        val branchesRefs = branches
            .distinct()
            .map { "$it:$it" }
            .toTypedArray()

        execute(projectFile, "push", "--atomic", "--tags", "--recurse-submodules=on-demand", remote, *branchesRefs)
    }

    override fun tag(projectFile: ProjectFile<T>, version: Version) {
        execute(projectFile, "tag", "-a", version.toString(), "-m", "v$version")
    }

    companion object {
        val GIT_EXECUTABLE =
            if (isWindows()) {
                "git.exe"
            } else {
                "git"
            }

        private val LINE_BREAK_REGEX = "\r?\n".toRegex()
    }

}
