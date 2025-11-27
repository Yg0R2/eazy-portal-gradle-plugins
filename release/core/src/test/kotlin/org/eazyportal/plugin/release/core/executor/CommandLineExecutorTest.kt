package org.eazyportal.plugin.release.core.executor

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.eazyportal.plugin.release.core.executor.exception.CliExecutionException
import org.eazyportal.plugin.release.core.project.FileSystemProjectFile
import org.eazyportal.plugin.release.core.project.ProjectFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import org.junit.jupiter.api.io.TempDir
import java.io.File

@TestInstance(PER_CLASS)
class CommandLineExecutorTest {

    @TempDir
    private lateinit var workingDir: File

    private lateinit var projectFile: ProjectFile<File>

    private val underTest = CommandLineExecutor()

    @BeforeEach
    fun setUp() {
        projectFile = FileSystemProjectFile(workingDir)
    }

    @EnabledOnOs(WINDOWS)
    @Test
    fun test_cliExecute_windows() {
        // GIVEN
        // WHEN
        val actual = underTest.execute(this.projectFile, "ping", "127.0.0.1", "-n", "1")

        // THEN
        assertThat(actual).contains("Packets: Sent = 1, Received = 1, Lost = 0 (0% loss)")
    }

    @DisabledOnOs(WINDOWS)
    @Test
    fun test_cliExecute_linux() {
        // GIVEN
        // WHEN
        val actual = underTest.execute(this.projectFile, "ping", "127.0.0.1", "-c", "1")

        // THEN
        assertThat(actual).contains("1 packets transmitted, 1 received, 0% packet loss, time 0ms")
    }

    @EnabledOnOs(WINDOWS)
    @Test
    fun test_cliExecute_shouldFail_whenCommandFails_windows() {
        // GIVEN
        // WHEN & THEN
        assertThatThrownBy { underTest.execute(this.projectFile, "ping") }
            .isInstanceOf(CliExecutionException::class.java)
            .hasMessageContaining("Usage: ping")
    }

    @DisabledOnOs(WINDOWS)
    @Test
    fun test_cliExecute_shouldFail_whenCommandFails_linux() {
        // GIVEN
        // WHEN& THEN
        assertThatThrownBy { underTest.execute(this.projectFile, "ping") }
            .isInstanceOf(CliExecutionException::class.java)
            .hasMessageContaining("ping: usage error: Destination address required")
    }

}
