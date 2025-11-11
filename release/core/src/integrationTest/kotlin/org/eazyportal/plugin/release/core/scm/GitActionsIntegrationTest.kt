package org.eazyportal.plugin.release.core.scm

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.BaseIntegrationTest
import org.eazyportal.plugin.release.core.executor.CommandLineExecutor
import org.eazyportal.plugin.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_001
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_002
import org.eazyportal.plugin.release.core.model.VersionFixtures.RELEASE_003
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions.Companion.GIT_EXECUTABLE
import org.eazyportal.plugin.release.core.scm.ScmConstants.MAIN_BRANCH
import org.eazyportal.plugin.release.core.scm.ScmConstants.REMOTE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GitActionsIntegrationTest : BaseIntegrationTest() {

    private val commandExecutor = CommandLineExecutor()

    private val underTest = GitActions(commandExecutor)

    @BeforeEach
    fun setUpRepository() {
        assertThat(git("init", "--initial-branch=$MAIN_BRANCH"))
            .contains("Initialized empty Git repository in ${projectFile.getFile().absolutePath}")

        projectFile
            .resolve(".gitignore")
            .writeText("build/")

        git("add", ".")
        git("commit", "-m", "initial commit")
    }

    @Test
    fun test_add() {
        // GIVEN
        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")

        // WHEN
        underTest.add(projectFile, "README.adoc")

        // THEN
        assertThat(git("status"))
            .contains("new file:   README.adoc")
    }

    @Test
    fun test_checkout() {
        // GIVEN
        git("checkout", "-b", "tmp")

        assertThat(git("status").split(LINE_BREAK_REGEX))
            .contains("On branch tmp")

        // WHEN
        underTest.checkout(projectFile, MAIN_BRANCH)

        // THEN
        assertThat(git("status").split(LINE_BREAK_REGEX))
            .contains("On branch $MAIN_BRANCH")
    }

    @Test
    fun test_clean() {
        // GIVEN
        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")

        // WHEN
        underTest.clean(projectFile)

        // THEN
        assertThat(git("status"))
            .contains("nothing to commit, working tree clean")
        assertThat(git("log"))
            .startsWith("commit")
            .endsWith("initial commit")
    }

    @Test
    fun test_commit() {
        // GIVEN
        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")

        // WHEN
        underTest.commit(projectFile, "docs: add README")

        // THEN
        assertThat(git("status"))
            .contains("nothing to commit, working tree clean")
        assertThat(git("log"))
            .startsWith("commit")
            .contains("docs: add README")
            .endsWith("initial commit")
    }

    @Test
    fun test_execute() {
        // GIVEN
        // WHEN & THEN
        assertThat(underTest.execute(projectFile, "help"))
            .contains("usage: git")
    }

    @Test
    fun test_fetch() {
        // GIVEN (origin repository)
        val originProjectFile = workingDir
            .resolve("origin-project")
            .also { it.mkdir() }
            .let(::FileSystemProjectFile)

        projectFile.getFile().copyRecursively(originProjectFile.getFile())

        originProjectFile
            .resolve("README.adoc")
            .writeText("Hello World!")

        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "add", ".")
        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "commit", "-m", "docs: add README")
        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "tag", RELEASE_001.toString())

        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "checkout", "-b", "tmp")

        originProjectFile
            .resolve("README.adoc")
            .writeText("Hello World!\nHello World!")
        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "add", ".")
        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "commit", "-m", "docs: update README")

        commandExecutor
            .execute(originProjectFile, GIT_EXECUTABLE, "branch")
            .split(LINE_BREAK_REGEX)
            .run { assertThat(this).containsExactly(MAIN_BRANCH, "* tmp") }

        // and GIVEN (add remote origin)
        git("remote", "add", REMOTE, originProjectFile.getFile().absolutePath)

        assertThat(git("remote", "-v").split(LINE_BREAK_REGEX))
            .containsExactly(
                "origin\t${originProjectFile.getFile().absolutePath} (fetch)",
                "origin\t${originProjectFile.getFile().absolutePath} (push)",
            )

        git("tag")
            .split(LINE_BREAK_REGEX)
            .filter { it.isNotBlank() }
            .run { assertThat(this).isEmpty() }

        assertThat(git("log", "--pretty=format:%s").split(LINE_BREAK_REGEX))
            .containsExactly("initial commit")

        // WHEN
        underTest.fetch(projectFile, REMOTE)

        // THEN
        git("branch")
            .split(LINE_BREAK_REGEX)
            .run { assertThat(this).containsExactly("* $MAIN_BRANCH") }

        git("tag")
            .split(LINE_BREAK_REGEX)
            .filter { it.isNotBlank() }
            .run { assertThat(this).containsExactly(RELEASE_001.toString()) }

        assertThat(git("log", "--pretty=format:%s").split(LINE_BREAK_REGEX))
            .containsExactly("docs: add README", "initial commit")
    }

    @Test
    fun test_getCommits() {
        // GIVEN
        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")
        git("commit", "-m", "docs: add README")

        projectFile
            .resolve("gradle.properties")
            .writeText("version = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: add gradle.properties")

        val commitHashes = git("log", "--pretty=format:%H")
            .split(LINE_BREAK_REGEX)

        // WHEN & THEN (get all commits)
        assertThat(underTest.getCommits(projectFile))
            .containsExactly("build: add gradle.properties", "docs: add README", "initial commit")

        // and WHEN & THEN (get commits between the last one and the HEAD)
        assertThat(underTest.getCommits(projectFile = projectFile, fromRef = commitHashes[0]))
            .isEmpty()

        // and WHEN & THEN (get commits between the second to latest and the HEAD)
        assertThat(underTest.getCommits(projectFile = projectFile, fromRef = commitHashes[1]))
            .containsExactly("build: add gradle.properties")

        // and WHEN & THEN (get commits between the oldest and the HEAD)
        assertThat(underTest.getCommits(projectFile = projectFile, fromRef = commitHashes[2]))
            .containsExactly("build: add gradle.properties", "docs: add README")

        // and WHEN & THEN (get commits between the last and the second to last)
        assertThat(underTest.getCommits(projectFile = projectFile, fromRef = commitHashes[2], toRef = commitHashes[1]))
            .containsExactly("docs: add README")

        // and WHEN & THEN (get commits between the last and the latest one)
        assertThat(underTest.getCommits(projectFile = projectFile, fromRef = commitHashes[2], toRef = commitHashes[0]))
            .containsExactly("build: add gradle.properties", "docs: add README")
    }

    @Test
    fun test_getLastTag() {
        // GIVEN
        git("tag", RELEASE_001.toString())

        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")
        git("commit", "-m", "docs: add README")

        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!\nHello World!")
        git("add", ".")
        git("commit", "-m", "docs: update README")

        git("tag", RELEASE_002.toString())

        projectFile
            .resolve("gradle.properties")
            .writeText("version = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: add gradle.properties")

        projectFile
            .resolve("gradle.properties")
            .writeText("group = org.eazyportal.dummy\nversion = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: update gradle.properties")

        git("tag", RELEASE_003.toString())

        val commitHashes = git("log", "--pretty=format:%H")
            .split(LINE_BREAK_REGEX)

        // WHEN & THEN (get last tag)
        assertThat(underTest.getLastTag(projectFile))
            .isEqualTo(RELEASE_003.toString())

        // and WHEN & THEN (get tag from tagged commit)
        assertThat(underTest.getLastTag(projectFile, commitHashes[0]))
            .isEqualTo(RELEASE_003.toString())
        assertThat(underTest.getLastTag(projectFile, commitHashes[2]))
            .isEqualTo(RELEASE_002.toString())
        assertThat(underTest.getLastTag(projectFile, commitHashes[4]))
            .isEqualTo(RELEASE_001.toString())

        // and WHEN & THEN (get tag from the closest commit)
        assertThat(underTest.getLastTag(projectFile, commitHashes[1]))
            .isEqualTo(RELEASE_002.toString())
        assertThat(underTest.getLastTag(projectFile, commitHashes[3]))
            .isEqualTo(RELEASE_001.toString())
    }

    @Test
    fun test_getSubmodules() {
        // GIVEN (origin repository)
        val originProjectFile = workingDir
            .resolve("submodule-project")
            .also { it.mkdir() }
            .let(::FileSystemProjectFile)

        commandExecutor
            .execute(originProjectFile, GIT_EXECUTABLE, "init", "-b", MAIN_BRANCH)
            .run {
                assertThat(this)
                    .contains("Initialized empty Git repository in ${originProjectFile.getFile().absolutePath}")
            }
        originProjectFile
            .resolve(".gitignore")
            .writeText("build/")

        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "add", ".")
        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "commit", "-m", "initial commit")

        // and GIVEN (add submodule)
        git(
            "-c",
            "protocol.file.allow=always",
            "submodule",
            "add",
            "--name", // TODO: git ignores this
            "dummy-submodule",
            originProjectFile.resolve(".git").getFile().absolutePath,
        ).run {
            assertThat(split(LINE_BREAK_REGEX))
                .containsExactly(
                    "Cloning into '${projectFile.getFile().absolutePath}/submodule-project'...",
                    "done."
                )
        }

        // WHEN & THEN
        assertThat(underTest.getSubmodules(projectFile))
            .containsExactly("submodule-project")
    }

    @Test
    fun test_getTags() {
        // GIVEN
        git("tag", RELEASE_001.toString())

        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")
        git("commit", "-m", "docs: add README")

        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!\nHello World!")
        git("add", ".")
        git("commit", "-m", "docs: update README")

        git("tag", RELEASE_002.toString())

        projectFile
            .resolve("gradle.properties")
            .writeText("version = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: add gradle.properties")

        projectFile
            .resolve("gradle.properties")
            .writeText("group = org.eazyportal.dummy\nversion = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: update gradle.properties")

        git("tag", RELEASE_003.toString())

        val commitHashes = git("log", "--pretty=format:%H")
            .split(LINE_BREAK_REGEX)

        // WHEN & THEN (get all tags)
        assertThat(underTest.getTags(projectFile))
            .containsExactly(RELEASE_003.toString())

        // WHEN & THEN (get tags from tagged commit)
        assertThat(underTest.getTags(projectFile, commitHashes[0]))
            .containsExactly(RELEASE_003.toString())
        assertThat(underTest.getTags(projectFile, commitHashes[2]))
            .containsExactly(RELEASE_002.toString(), RELEASE_003.toString())
        assertThat(underTest.getTags(projectFile, commitHashes[4]))
            .containsExactly(RELEASE_001.toString(), RELEASE_002.toString(), RELEASE_003.toString())

        // and WHEN & THEN (get tags from the closest commit)
        assertThat(underTest.getTags(projectFile, commitHashes[1]))
            .containsExactly(RELEASE_003.toString())
        assertThat(underTest.getTags(projectFile, commitHashes[3]))
            .containsExactly(RELEASE_002.toString(), RELEASE_003.toString())
    }

    @Test
    fun test_mergeNoCommit() {
        // GIVE
        git("checkout", "-b", "tmp")

        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")
        git("commit", "-m", "docs: add README")

        git("checkout", MAIN_BRANCH)

        // WHEN
        underTest.mergeNoCommit(projectFile, "tmp")

        // THEN
        assertThat(git("status").split(LINE_BREAK_REGEX))
            .contains(
                "On branch $MAIN_BRANCH",
                "All conflicts fixed but you are still merging.",
                "Changes to be committed:",
                "\tnew file:   README.adoc",
            )
    }

    @Test
    fun test_push() {
        // GIVEN (bare origin repository)
        val originProjectFile = workingDir
            .resolve("origin-project.git")
            .also { it.mkdir() }
            .let(::FileSystemProjectFile)

        commandExecutor.execute(originProjectFile, GIT_EXECUTABLE, "init", "--bare", "-b", MAIN_BRANCH)

        // and GIVEN (another local repository)
        val validateProjectFile = workingDir
            .resolve("validate-project")
            .let(::FileSystemProjectFile)

        commandExecutor.execute(
            FileSystemProjectFile(workingDir),
            GIT_EXECUTABLE,
            "clone",
            originProjectFile.getFile().absolutePath,
            "validate-project",
        )

        // and GIVEN (add remote origin)
        git("remote", "add", REMOTE, originProjectFile.getFile().absolutePath)

        assertThat(git("remote", "-v").split(LINE_BREAK_REGEX))
            .containsExactly(
                "origin\t${originProjectFile.getFile().absolutePath} (fetch)",
                "origin\t${originProjectFile.getFile().absolutePath} (push)",
            )

        // and GIVEN (add commit and tag to local repository)
        projectFile
            .resolve("README.adoc")
            .writeText("Hello World!")
        git("add", ".")
        git("commit", "-m", "docs: add README")

        git("tag", RELEASE_001.toString())

        projectFile
            .resolve("gradle.properties")
            .writeText("version = 0.0.1-SNAPSHOT")
        git("add", ".")
        git("commit", "-m", "build: add gradle.properties")

        // WHEN & THEN (push tags only)
        underTest.push(projectFile, REMOTE)

        assertThatThrownBy {
            commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "pull", "--tags", REMOTE)
        }.isInstanceOf(CliExecutionException::class.java)
            .hasMessageContaining(
                " * [new tag]         $RELEASE_001      -> $RELEASE_001",
                "Your configuration specifies to merge with the ref 'refs/heads/$MAIN_BRANCH'",
                "from the remote, but no such ref was fetched.",
            )

        commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "tag")
            .split(LINE_BREAK_REGEX)
            .run { assertThat(this).containsExactly(RELEASE_001.toString()) }

        assertThatThrownBy {
            commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "log", "--pretty=format:%s")
        }.isInstanceOf(CliExecutionException::class.java)
            .hasMessage("fatal: your current branch '$MAIN_BRANCH' does not have any commits yet")

        // WHEN & THEN (push also branch)
        underTest.push(projectFile, REMOTE, MAIN_BRANCH)

        commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "pull", "--tags", REMOTE)

        commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "tag")
            .split(LINE_BREAK_REGEX)
            .run { assertThat(this).containsExactly(RELEASE_001.toString()) }

        commandExecutor.execute(validateProjectFile, GIT_EXECUTABLE, "log", "--pretty=format:%s")
            .split(LINE_BREAK_REGEX)
            .run {
                assertThat(this).containsExactly(
                    "build: add gradle.properties",
                    "docs: add README",
                    "initial commit"
                )
            }
    }

    @Test
    fun test_tag() {
        // GIVEN
        git("tag")
            .split(LINE_BREAK_REGEX)
            .filter { it.isNotBlank() }
            .run { assertThat(this).isEmpty() }

        // WHEN
        underTest.tag(projectFile, RELEASE_001)

        // THEN
        assertThat(git("tag").split(LINE_BREAK_REGEX))
            .containsExactly(RELEASE_001.toString())
    }

    private fun git(vararg args: String): String =
        commandExecutor.execute(projectFile, GIT_EXECUTABLE, *args)

    companion object {
        private val LINE_BREAK_REGEX = "\r?\n".toRegex()
    }

}
