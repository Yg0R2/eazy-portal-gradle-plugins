package org.eazyportal.plugin.release.core.scm

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.executor.CommandExecutor
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.eazyportal.plugin.release.core.scm.GitActions.Companion.GIT_EXECUTABLE
import org.eazyportal.plugin.release.core.scm.exception.ScmActionException
import org.eazyportal.plugin.release.core.version.model.Version
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File

@ExtendWith(MockKExtension::class)
class GitActionsTest {

    @TempDir
    private lateinit var workingDir: File

    private lateinit var projectFile: ProjectFile<File>

    @MockK
    private lateinit var commandExecutor: CommandExecutor<ProjectFile<File>>

    @InjectMockKs
    private lateinit var underTest: GitActions<File>

    @BeforeEach
    fun setUp() {
        projectFile = FileSystemProjectFile(workingDir)
    }

    @Test
    fun test_add() {
        // GIVEN
        val filePaths = arrayOf(".")

        every { commandExecutor.execute(projectFile, GIT_EXECUTABLE, "add", *filePaths) } returns ""

        // WHEN
        underTest.add(projectFile, *filePaths)

        // THEN

        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "add", *filePaths)
        }
    }

    @Test
    fun test_checkout() {
        // GIVEN
        val toRef = "main"

        every { commandExecutor.execute(projectFile, GIT_EXECUTABLE, "checkout", toRef) } returns ""

        // WHEN
        underTest.checkout(projectFile, toRef)

        // THEN
        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "checkout", toRef)
        }
    }

    @Test
    fun test_commit() {
        // GIVEN
        val message = "commit message"

        every { commandExecutor.execute(projectFile, GIT_EXECUTABLE, "commit", "-m", message) } returns ""

        // WHEN
        underTest.commit(projectFile, message)

        // THEN
        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "commit", "-m", message)
        }
    }

    @Test
    fun test_execute() {
        // GIVEN
        val response = "dummy response"

        every { commandExecutor.execute(projectFile, GIT_EXECUTABLE, "log") } returns response

        // WHEN
        val actual = underTest.execute(projectFile, "log")

        // THEN
        assertThat(actual).isEqualTo(response)

        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "log")
        }
    }

    @Test
    fun test_execute_shouldThrowException() {
        // GIVEN
        val errorMessage = "error message"

        every { commandExecutor.execute(projectFile, GIT_EXECUTABLE, "log") } throws RuntimeException(errorMessage)

        // WHEN & THEN
        assertThatThrownBy { underTest.execute(projectFile, "log") }
            .isInstanceOf(ScmActionException::class.java)
            .cause()
            .hasMessage(errorMessage)

        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "log")
        }
    }

    @Test
    fun test_fetch() {
        // GIVEN
        val remote = "origin"
        val currentBranchName = "branch"

        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "fetch",
                remote,
                "--tags",
                "--prune",
                "--prune-tags",
                "--recurse-submodules",
            )
        } returns ""

        every {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "rev-parse", "--abbrev-ref", "HEAD")
        } returns currentBranchName

        every {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "reset", "--hard", "$remote/$currentBranchName")
        } returns ""

        // WHEN
        underTest.fetch(projectFile, remote)

        // THEN
        verifySequence {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "fetch",
                remote,
                "--tags",
                "--prune",
                "--prune-tags",
                "--recurse-submodules",
            )
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "rev-parse", "--abbrev-ref", "HEAD")
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "reset", "--hard", "$remote/$currentBranchName")
        }
    }

    @Test
    fun test_getCommits() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "log",
                "--pretty=format:%s",
                "HEAD",
            )
        } returns "$COMMIT_MESSAGE_1\n$COMMIT_MESSAGE_2\r\n$COMMIT_MESSAGE_3"

        // WHEN
        val actual = underTest.getCommits(projectFile)

        // THEN
        assertThat(actual).isEqualTo(COMMIT_MESSAGES)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "log",
                "--pretty=format:%s",
                "HEAD",
            )
        }
    }

    @Test
    fun test_getCommits_withRefs() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "log",
                "--pretty=format:%s",
                "$COMMIT_HASH_1..$COMMIT_HASH_2",
            )
        } returns COMMIT_MESSAGES.joinToString(System.lineSeparator())

        // WHEN
        val actual = underTest.getCommits(projectFile, COMMIT_HASH_1, COMMIT_HASH_2)

        // THEN
        assertThat(actual).isEqualTo(COMMIT_MESSAGES)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "log",
                "--pretty=format:%s",
                "$COMMIT_HASH_1..$COMMIT_HASH_2",
            )
        }
    }

    @Test
    fun test_getCurrentBranch() {
        // GIVEN
        val currentBranchName = "branch"

        every {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "rev-parse", "--abbrev-ref", "HEAD")
        } returns currentBranchName

        // WHEN
        val actual = underTest.getCurrentBranch(projectFile)

        // THEN
        assertThat(actual).isEqualTo(currentBranchName)

        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "rev-parse", "--abbrev-ref", "HEAD")
        }
    }

    @Test
    fun test_getLastTag() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "describe",
                "--abbrev=0",
                "--tags",
                "HEAD",
            )
        } returns TAG_1

        // WHEN
        val actual = underTest.getLastTag(projectFile)

        // THEN
        assertThat(actual).isEqualTo(TAG_1)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "describe",
                "--abbrev=0",
                "--tags",
                "HEAD",
            )
        }
    }

    @Test
    fun test_getLastTag_withRef() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "describe",
                "--abbrev=0",
                "--tags",
                COMMIT_HASH_1,
            )
        } returns TAG_1

        // WHEN
        val actual = underTest.getLastTag(projectFile, COMMIT_HASH_1)

        // THEN
        assertThat(actual).isEqualTo(TAG_1)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "describe",
                "--abbrev=0",
                "--tags",
                COMMIT_HASH_1,
            )
        }
    }

    @Test
    fun test_getSubmodules() {
        // GIVEN
        val submodules = listOf("ui-project", "examples-project")
        val response = listOf(
            " 2ef7bde608ce5404e97d5f042f95f89f1c232871 ui-project (heads/main)",
            " 2ef7bde608ce5404e97d5f042f95f89f1c232871 examples-project (heads/main)",
        )

        every {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "submodule")
        } returns response.joinToString(System.lineSeparator())

        // WHEN
        val actual = underTest.getSubmodules(projectFile)

        // THEN
        assertThat(actual).isEqualTo(submodules)

        verify(exactly = 1) {
            commandExecutor.execute(projectFile, GIT_EXECUTABLE, "submodule")
        }
    }

    @Test
    fun test_getTags() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "--sort=-creatordate",
                "--contains",
                "HEAD",
            )
        } returns "$TAG_1\n$TAG_2\r\n$TAG_3"

        // WHEN
        val actual = underTest.getTags(projectFile)

        // THEN
        assertThat(actual).isEqualTo(TAGS)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "--sort=-creatordate",
                "--contains",
                "HEAD",
            )
        }
    }

    @Test
    fun test_getGitTags_withRef() {
        // GIVEN
        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "--sort=-creatordate",
                "--contains",
                COMMIT_HASH_1,
            )
        } returns TAGS.joinToString(System.lineSeparator())

        // WHEN
        val actual = underTest.getTags(projectFile, COMMIT_HASH_1)

        // THEN
        assertThat(actual).isEqualTo(TAGS)

        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "--sort=-creatordate",
                "--contains",
                COMMIT_HASH_1,
            )
        }
    }

    @Test
    fun test_mergeNoCommit() {
        // GIVEN
        val fromBranch = "main"

        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "merge",
                "--no-ff",
                "--no-commit",
                "--strategy-option=theirs",
                fromBranch,
            )
        } returns ""

        // WHEN
        underTest.mergeNoCommit(projectFile, fromBranch)

        // THEN
        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "merge",
                "--no-ff",
                "--no-commit",
                "--strategy-option=theirs",
                fromBranch,
            )
        }
    }

    @Test
    fun test_push() {
        // GIVEN
        val remote = "remote-repository"
        val branch = "release-branch"

        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "push",
                "--atomic",
                "--tags",
                "--recurse-submodules=on-demand",
                remote,
                "$branch:$branch",
            )
        } returns ""

        // WHEN
        underTest.push(projectFile, remote, branch)

        // THEN
        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "push",
                "--atomic",
                "--tags",
                "--recurse-submodules=on-demand",
                remote,
                "$branch:$branch",
            )
        }
    }

    @Test
    fun test_tag() {
        // GIVEN
        val version = Version.of("0.0.1")

        every {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "-a",
                version.toString(),
                "-m",
                "v$version",
            )
        } returns ""

        // WHEN
        underTest.tag(projectFile, version)

        // THEN
        verify(exactly = 1) {
            commandExecutor.execute(
                projectFile,
                GIT_EXECUTABLE,
                "tag",
                "-a",
                version.toString(),
                "-m",
                "v$version",
            )
        }
    }

    companion object {
        private const val COMMIT_HASH_1 = "hash-1"
        private const val COMMIT_HASH_2 = "hash-2"

        private const val COMMIT_MESSAGE_1 = "feature: commit"
        private const val COMMIT_MESSAGE_2 = "fix: commit"
        private const val COMMIT_MESSAGE_3 = "chore: commit"
        private val COMMIT_MESSAGES =
            listOf(COMMIT_MESSAGE_1, COMMIT_MESSAGE_2, COMMIT_MESSAGE_3)

        private const val TAG_1 = "0.1.1"
        private const val TAG_2 = "0.1.2"
        private const val TAG_3 = "0.1.3"
        private val TAGS = listOf(TAG_1, TAG_2, TAG_3)
    }

}
