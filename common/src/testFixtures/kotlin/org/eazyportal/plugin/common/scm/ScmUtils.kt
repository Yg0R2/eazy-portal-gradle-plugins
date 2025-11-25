package org.eazyportal.plugin.common.scm

import org.eazyportal.plugin.common.GradleTestFixtures.PROJECT_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.CHORE_COMMIT_MESSAGE
import org.eazyportal.plugin.common.ScmTestFixtures.DUMMY_FILE_NAME
import org.eazyportal.plugin.common.ScmTestFixtures.INITIAL_TAG
import org.eazyportal.plugin.common.cli.CommandLineUtils.git
import java.io.File
import java.util.UUID
import kotlin.sequences.forEach

object GitUtils : ScmUtils() {

    override fun checkout(projectDir: File, toBranch: String) {
        projectDir.git("checkout", toBranch)
    }

    override fun clean(projectDir: File) {
        // Workaround for using none-bare repository as origin
        projectDir.git("reset", "--hard")

        projectDir.git("clean", "-fdX")
    }

    override fun clone(from: File, to: File) {
        to.parentFile.git(
            "-c",
            "protocol.file.allow=always",
            "clone",
            "--recurse-submodules",
            from.resolve(".git").path,
            to.name,
        )
    }

    override fun createDummyCommit(
        projectDir: File,
        branch: String?,
        commitMessage: String,
    ) {
        if (branch != null) {
            checkout(projectDir, branch)
        }

        createDummyFile(projectDir)

        projectDir.git("add", ".")
        projectDir.git("commit", "-m", commitMessage)
    }

    override fun createDummyFile(projectDir: File) {
        projectDir.resolve(DUMMY_FILE_NAME)
            .writeText(UUID.randomUUID().toString())
    }

    override fun fetch(projectDir: File, remote: String, vararg branches: String) {
        projectDir.git("fetch", remote, "--tags", "--prune", "--prune-tags", "--recurse-submodules")

        val currentBranchName = projectDir.git("rev-parse", "--abbrev-ref", "HEAD")
        projectDir.git("reset", "--hard", "$remote/$currentBranchName")

        branches.ifEmpty { arrayOf("main", "dev") }
            .asSequence()
            .filter { it != currentBranchName }
            .forEach { projectDir.git("branch", "-f", it, "$remote/$it") }
    }

    override fun getCommits(
        projectDir: File,
        fromRef: String?,
        toRef: String
    ): List<String> =
        listOfNotNull(fromRef, toRef)
            .joinToString("..")
            .let { projectDir.git("log", "--pretty=format:%s", it) }
            .splitOutput()

    override fun getLastTag(projectDir: File, fromRef: String?): String =
        projectDir.git("describe", "--abbrev=0", "--tags", fromRef ?: "HEAD")
            .trim()

    override fun getTags(projectDir: File): List<String> =
        projectDir.git("tag", "--list", "--sort=-creatordate")
            .splitOutput()

    override fun initializeRepository(projectDir: File) {
        projectDir.resolve("README.adoc")
            .writeText("= EazyPortal - $PROJECT_NAME")

        projectDir.git("init", "--initial-branch=main")
        projectDir.git("add", ".")
        projectDir.git("commit", "-m", "initial commit")

        projectDir.git("tag", INITIAL_TAG)

        // TODO: move somewhere for origin specific
        // Workaround for using none-bare repository as origin
        projectDir.git("config", "receive.denyCurrentBranch", "ignore")

        // Workaround to have both main and dev branches
        projectDir.git("branch", "dev")
    }

    override fun status(projectDir: File): List<String> =
        projectDir.git("status")
            .splitOutput()

    private fun String.splitOutput(): List<String> =
        split(System.lineSeparator())
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()

}

abstract class ScmUtils {

    abstract fun checkout(projectDir: File, toBranch: String)

    abstract fun clean(projectDir: File)

    abstract fun clone(from: File, to: File)

    abstract fun createDummyCommit(
        projectDir: File,
        branch: String? = null,
        commitMessage: String = CHORE_COMMIT_MESSAGE,
    )

    abstract fun createDummyFile(projectDir: File)

    abstract fun fetch(
        projectDir: File,
        remote: String,
        vararg branches: String,
    )

    abstract fun getCommits(
        projectDir: File,
        fromRef: String? = null,
        toRef: String = "HEAD",
    ): List<String>

    abstract fun getLastTag(projectDir: File, fromRef: String? = null): String

    abstract fun getTags(projectDir: File): List<String>

    abstract fun initializeRepository(projectDir: File)

    abstract fun status(projectDir: File): List<String>

}
