package org.eazyportal.gradle.eazysettings

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazysettings.EazySettingsPlugin.Companion.EAZY_SETTINGS_EXTENSION_NAME
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

/**
 * Black-box coverage for `org.eazyportal.gradle.eazy-settings` (design §6.3/§6.5, TOOLS-68 acceptance criteria):
 * real Gradle builds, driven through TestKit's plugin-under-test classpath,
 * applying the plugin by bare id in a synthetic receiver's `settings.gradle.kts` `plugins { }` block —
 * the normal path a receiver repo uses.
 *
 * Subprojects are named outside the standard archetype set (design §5.2) on purpose:
 * they fall back to `ProjectType.DEFAULT` / `kotlin-project-convention`,
 * which needs no `eazyportal-core` repository to resolve —
 * keeping these tests hermetic while still exercising the auto-apply + version-resolution behavior under test here.
 */
class EazySettingsPluginFunctionalTest {

    @Test
    fun `every subproject gets eazy-project auto-applied, the root does not`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b")

        val output = runGradleTask(
            projectDir,
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
        )

        assertThat(output)
            .contains("eazy-project diagnostics — :extras-a")
            .contains("eazy-project diagnostics — :extras-b")

        val rootOutput = runFailingGradleTask(projectDir, ":$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(rootOutput).contains("not found in root project")
    }

    @Test
    fun `eazy-project's own default applies when the DSL override is absent`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras")

        val output = runGradleTask(projectDir, ":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION")
    }

    @Test
    fun `the eazySettings override wins over eazy-project's own default`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras") {
            settings {
                script {
                    generateEazySettingsString("9.9.9")
                }
            }
        }

        val output = runGradleTask(projectDir, ":extras:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        assertThat(output).contains("eazyPortalCoreVersion  : 9.9.9")
    }

    @Test
    fun `CC double-run - override wins, the 2nd run reuses the CC entry, and zero CC problems occur`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir, "extras-a", "extras-b") {
            settings {
                script {
                    generateEazySettingsString("9.9.9")
                }
            }
        }

        val firstRun = runGradleTask(
            projectDir,
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(firstRun)
            .contains("Configuration cache entry stored.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")

        val secondRun = runGradleTask(
            projectDir,
            ":extras-a:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            ":extras-b:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "--configuration-cache",
            "--configuration-cache-problems=fail",
        )
        assertThat(secondRun)
            .contains("Reusing configuration cache.")
            .contains("eazyPortalCoreVersion  : 9.9.9")
            .doesNotContain("problem was found", "problems were found")
    }

    @Test
    fun `a module declaring its own repositories fails the build (FAIL_ON_PROJECT_REPOS)`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir) {
            subproject("extras") {
                useMavenCentral()
            }
        }

        val output = runFailingGradleTask(projectDir, "help")

        assertThat(output).contains("Build was configured to prefer settings repositories over project repositories")
    }

    @Test
    fun `the hermetic stub resolves eazyportal-core with no credentials — the real GitHub repo is registered but never hit`(
        @TempDir workingDir: File,
    ) {
        val exampleRepositoryDir = File(workingDir, "repository")
            .also { Files.createDirectories(it.toPath()) }
            .also { writeExampleCoreStubRepo(it) }

        val projectDir = buildExampleProject(workingDir, "common") {
            settings {
                script {
                    """
                    dependencyResolutionManagement {
                        repositories {
                            exclusiveContent {
                                forRepository {
                                    maven { url = uri("${exampleRepositoryDir.toURI()}") }
                                }

                                filter {
                                    includeGroup("org.eazyportal.core")
                                }
                            }
                        }
                    }
                    """.trimIndent()
                }
            }
        }

        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val output = runGradleTask(
            projectDir,
            ":common:dependencies", "--configuration", "compileClasspath",
            environment = hermeticEnvironmentWithFakeCredentials(gradleHomeDir),
        )

        assertThat(output).contains("org.eazyportal.core:eazyportal-core-common:$EXAMPLE_CORE_DEFAULT_VERSION")
    }

    @Test
    fun `forcing resolution from the real GitHub repo without credentials fails with a clear error, never silent anonymous`(
        @TempDir workingDir: File,
    ) {
        val gradleHomeDir = File(workingDir, "gradle-home")
            .also { it.mkdirs() }

        val projectDir = buildExampleProject(workingDir, "common")

        val output = runFailingGradleTask(
            projectDir,
            ":common:dependencies", "--configuration", "compileClasspath",
            environment = hermeticEnvironment(gradleHomeDir),
        )

        assertThat(output)
            .contains("The following Gradle properties are missing for 'GitHubPackages' credentials")
            .doesNotContain("BUILD SUCCESSFUL")
    }

    /** Redirects `GRADLE_USER_HOME` into an isolated, empty [gradleUserHome] and strips ambient `GitHubPackages` credential env vars — so the build never sees the real machine's `~/.gradle` state (creds, caches). */
    private fun hermeticEnvironment(gradleUserHome: File): Map<String, String> =
        System.getenv() -
                "ORG_GRADLE_PROJECT_GitHubPackagesUsername" -
                "ORG_GRADLE_PROJECT_GitHubPackagesPassword" +
                ("GRADLE_USER_HOME" to gradleUserHome.absolutePath)

    /**
     * [hermeticEnvironment] plus FAKE, non-functional `GitHubPackages` credentials.
     * Gradle validates that `credentials(PasswordCredentials::class)` values are *present* for every registered
     * repository requiring them eagerly, for the whole resolution session — regardless of whether that repository
     * is ever actually queried. The stub repo is scoped with `exclusiveContent { }` (see [generatePluginString])
     * so the real GitHub host is never contacted; these fake values exist purely to satisfy that eager presence check.
     */
    private fun hermeticEnvironmentWithFakeCredentials(gradleUserHome: File): Map<String, String> =
        hermeticEnvironment(gradleUserHome) +
            ("ORG_GRADLE_PROJECT_GitHubPackagesUsername" to "stub-user") +
            ("ORG_GRADLE_PROJECT_GitHubPackagesPassword" to "stub-token")

    /**
     * Hand-written minimal `org.eazyportal.core:eazyportal-core-{bom,common,test}:[EXAMPLE_CORE_DEFAULT_VERSION]` artifacts in a Maven2-layout
     * directory — the smallest possible stand-in for the workspace `repository/` stub (design §7.4), just enough for a real
     * `compileClasspath` to resolve (a `platform()`-only POM for the BOM, POM + an empty-but-valid jar for the two libraries
     * actually placed on a configuration). Mirrors [org.eazyportal.gradle.eazyproject.EazyProjectPluginFunctionalTest]'s own stub —
     * duplicated rather than shared, same as that test's private helper.
     */
    private fun writeExampleCoreStubRepo(repoDir: File) {
        writeArtifact(repoDir, "eazyportal-core-bom", "pom", listOf("eazyportal-core-common", "eazyportal-core-test"))
        writeArtifact(repoDir, "eazyportal-core-common", "jar")
        writeArtifact(repoDir, "eazyportal-core-test", "jar")
    }

    private fun writeArtifact(
        repoDir: File,
        artifactId: String,
        packaging: String,
        managedArtifactIds: List<String> = emptyList(),
    ) {
        val artifactDir = File(repoDir, "org/eazyportal/core/$artifactId/$EXAMPLE_CORE_DEFAULT_VERSION").also { it.mkdirs() }

        val dependencies = managedArtifactIds.joinToString("\n") {
            """
            <dependency>
                <groupId>org.eazyportal.core</groupId>
                <artifactId>$it</artifactId>
                <version>$EXAMPLE_CORE_DEFAULT_VERSION</version>
            </dependency>
            """
        }

        File(artifactDir, "$artifactId-$EXAMPLE_CORE_DEFAULT_VERSION.pom").writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>org.eazyportal.core</groupId>
                <artifactId>$artifactId</artifactId>
                <version>$EXAMPLE_CORE_DEFAULT_VERSION</version>
                <packaging>$packaging</packaging>
                <dependencyManagement>
                    <dependencies>
                        $dependencies
                    </dependencies>
                </dependencyManagement>
            </project>
            """.trimIndent()
        )

        if (packaging.equals("jar", ignoreCase = true)) {
            // The 22-byte "end of central directory" record alone is a valid, empty ZIP/JAR.
            File(artifactDir, "$artifactId-$EXAMPLE_CORE_DEFAULT_VERSION.jar").writeBytes(
                byteArrayOf(0x50, 0x4b, 0x05, 0x06) + ByteArray(18),
            )
        }
    }

    /**
     * A synthetic receiver: an empty root applying `org.eazyportal.gradle.eazy-settings` via the settings `plugins { }`
     * block (the real receiver bootstrap, README Quickstart) + one subproject per [subprojectNames], optionally
     * overriding `eazyPortalCoreVersion` via the `eazySettings { }` DSL.
     */
    private fun buildExampleProject(
        workingDir: File,
        vararg subprojectNames: String,
        builder: ExampleProjectBuilder.() -> Unit =  { },
    ): File =
        exampleProject(workingDir) {
            settings {
                plugins("org.eazyportal.gradle.eazy-settings")
            }

            subprojectNames.forEach {
                subproject(it)
            }

            builder()
        }

    private fun generateEazySettingsString(eazyPortalCoreVersionOverride: String): String =
        """
        $EAZY_SETTINGS_EXTENSION_NAME {
            eazyPortalCoreVersion = "$eazyPortalCoreVersionOverride"
        }
        """.trimIndent()

}
