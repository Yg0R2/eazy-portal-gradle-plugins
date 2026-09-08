package org.eazyportal.gradle.internal.project

import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.ARTIFACT_ID
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.DESCRIPTION
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.GROUP_ID
import java.io.File

/**
 * Fluent builder for a synthetic Gradle project scaffolded into a working directory and driven by the TestKit helpers in [org.eazyportal.gradle.internal.GradleUtils].
 * Each `with…` mutator returns `this` for chaining; the terminal [build] materializes the files and returns the project directory.
 *
 * Only the parts that differ between tests need to be declared — everything else has a sensible default for a minimal Gradle project.
 */
class ExampleProjectBuilder(
    private val workingDir: File,
) {

    private var rootProjectName: String = ARTIFACT_ID
    private val plugins = mutableListOf<String>()
    private var group: String = GROUP_ID
    private var description: String = DESCRIPTION
    private var mavenCentral: Boolean = false
    private val buildScriptBlocks = mutableListOf<String>()
    private val settingsScriptBlocks = mutableListOf<String>()
    private val sourceFiles = mutableListOf<SourceFile>()

    /** Overrides the root project name (`settings.gradle.kts`). */
    fun withRootProjectName(name: String): ExampleProjectBuilder =
        apply { rootProjectName = name }

    /**
     * Adds plugins to the `plugins {}` block;
     * - each plugin that contains '.' is emitted as `id("…")`.
     * - each plugin that does not contain '.', but contains '-' is emitted as `` `…` ``.
     * - any other plugin is emitted as-is (`…`).
     */
    fun withBuildPlugins(vararg plugins: String): ExampleProjectBuilder =
        apply {
            plugins.forEach {
                if (it.contains(".")) {
                    this.plugins += "id(\"$it\")"
                } else if (it.contains("-")) {
                    this.plugins += "`$it`"
                } else {
                    this.plugins += it
                }
            }
        }

    /** Sets the project `group`. */
    fun withGroup(group: String): ExampleProjectBuilder =
        apply { this.group = group }

    /** Sets the project `description`. */
    fun withDescription(description: String): ExampleProjectBuilder =
        apply { this.description = description }

    /** Adds a `repositories { mavenCentral() }` block (e.g. to resolve `kotlin-stdlib`). */
    fun withMavenCentral(): ExampleProjectBuilder =
        apply { mavenCentral = true }

    /** Appends a raw block to `build.gradle.kts` (e.g. `gradlePlugin {}`, `publishing {}`). */
    fun withBuildScript(scriptBlock: () -> String): ExampleProjectBuilder =
        apply { buildScriptBlocks += scriptBlock().trimIndent() }

    /** Writes a custom Java source under `src/[sourceSet]/java/[classPath]`. */
    fun withJavaSource(
        sourceSet: String = "main",
        classPath: String = "org/eazyportal/example/Example.java",
        contentBlock: () -> String = { EXAMPLE_JAVA_SOURCE },
    ): ExampleProjectBuilder =
        apply { sourceFiles += SourceFile("src/$sourceSet/java/$classPath", contentBlock().trimIndent()) }

    /** Writes a custom Kotlin source under `src/[sourceSet]/kotlin/[classPath]`. */
    fun withKotlinSource(
        sourceSet: String = "main",
        classPath: String = "org/eazyportal/example/Example.kt",
        contentBlock: () -> String = { EXAMPLE_KOTLIN_SOURCE },
    ): ExampleProjectBuilder =
        apply { sourceFiles += SourceFile("src/$sourceSet/kotlin/$classPath", contentBlock().trimIndent()) }

    /** Appends a raw block to `settings.gradle.kts` (e.g. `dependencyResolutionManagement {}`). */
    fun withSettingsScript(scriptBlock: () -> String): ExampleProjectBuilder =
        apply { settingsScriptBlocks += scriptBlock().trimIndent() }

    /**
     * Materializes `settings.gradle.kts`, `build.gradle.kts`, and any source files.
     *
     * @return the project dir.
     */
    fun build(): File {
        File(workingDir, "settings.gradle.kts").writeText(generateSettingsGradleKtsContent())

        File(workingDir, "build.gradle.kts").writeText(generateBuildGradleKtsContent())

        sourceFiles.forEach { source ->
            File(workingDir, source.relativePath)
                .also { it.parentFile.mkdirs() }
                .writeText(source.content)
        }

        return workingDir
    }

    private fun generateBuildGradleKtsContent(): String =
        buildList {
            add(
                buildString {
                    appendLine("plugins {")
                    plugins.forEach { appendLine("    $it") }
                    append("}")
                },
            )
            add("group = \"$group\"")
            add("description = \"$description\"")
            if (mavenCentral) {
                add(
                    """
                    repositories {
                        mavenCentral()
                    }
                    """.trimIndent(),
                )
            }
            addAll(buildScriptBlocks)
        }.joinToString(separator = "\n\n", postfix = "\n")

    private fun generateSettingsGradleKtsContent(): String =
        buildList {
            add(
                """
                rootProject.name = "$rootProjectName"
                """.trimIndent()
            )
            addAll(settingsScriptBlocks)
        }.joinToString(separator = "\n\n", postfix = "\n")

    private data class SourceFile(
        val relativePath: String,
        val content: String,
    )

    companion object {
        private val EXAMPLE_JAVA_SOURCE =
            """
            package org.eazyportal.example;

            public class Example {
                public String greetings() {
                    return "Hello World!";
                }
            }
            """.trimIndent()

        private val EXAMPLE_KOTLIN_SOURCE =
            """
            package org.eazyportal.example

            fun greetings(): String =
                "Hello World!"
            """.trimIndent()
    }

}
