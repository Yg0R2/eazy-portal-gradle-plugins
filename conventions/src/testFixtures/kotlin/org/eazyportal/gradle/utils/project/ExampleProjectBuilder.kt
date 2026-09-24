package org.eazyportal.gradle.utils.project

import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_GROUP_ID
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_DESCRIPTION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import java.io.File

/**
 * Kotlin DSL for a synthetic Gradle project scaffolded into a working directory and driven by the
 * TestKit helpers in [org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder].
 *
 * Only the parts that differ between tests need to be declared — everything else has a sensible default
 * for a minimal Gradle project. The files are written as soon as the configure block returns.
 *
 * ```
 * val projectDir = exampleProject(workingDir) {
 *     settings {
 *         plugins("org.eazyportal.gradle.eazy-settings")
 *         rootProjectName = "receiver"
 *     }
 *
 *     rootProject {
 *         plugins("org.eazyportal.gradle.kotlin-project-convention")
 *         useMavenCentral()
 *         kotlinSource { ... }
 *     }
 *
 *     subproject("extras") {
 *         plugins("org.eazyportal.gradle.eazy-project")
 *         javaSource { ... }
 *     }
 * }
 * ```
 */
@ExampleProjectDsl
class ExampleProjectBuilder private constructor(
    workingDir: File,
) {

    private val projectDir = workingDir
        .resolve(EXAMPLE_ROOT_PROJECT_NAME)
        .also { it.mkdirs() }

    private val settingsBuilder = SettingsBuilder(projectDir)
    private val rootProjectBuilder = RootProjectBuilder(projectDir)
    private val subprojectBuilders = linkedMapOf<String, SubprojectBuilder>()

    /**
     * Declares the `settings.gradle.kts` file, configured with its own [SettingsBuilder].
     */
    fun settings(configure: SettingsBuilder.() -> Unit) {
        settingsBuilder.configure()
    }

    /**
     * Declares the rootProject, configured with its own [RootProjectBuilder] rooted at `projectDir`.
     */
    fun rootProject(configure: RootProjectBuilder.() -> Unit) {
        rootProjectBuilder.configure()
    }

    /**
     * Declares a subproject `include("name")`d from the root, configured with its own [SubprojectBuilder] rooted at `projectDir/name`.
     * Only one level deep — a subproject cannot itself declare further subprojects.
     */
    fun subproject(
        name: String,
        configure: SubprojectBuilder.() -> Unit = {},
    ) {
        subprojectBuilders.getOrPut(name) {
            SubprojectBuilder(File(projectDir, name))
        }.configure()
    }

    private fun build(): File {
        settingsBuilder.build(subprojectBuilders.keys.toList())

        rootProjectBuilder.build()

        subprojectBuilders.values.forEach { it.build() }

        return projectDir
    }

    @ExampleProjectDsl
    abstract class CommonBuilder {

        private val plugins = mutableListOf<String>()
        private val scriptBlocks = mutableListOf<String>()

        /**
         * Adds plugins to the `plugins {}` block;
         * - each plugin that contains '.' is emitted as `id("…")`.
         * - each plugin that does not contain '.', but contains '-' is emitted as `` `…` ``.
         * - any other plugin is emitted as-is (`…`).
         */
        fun plugins(vararg plugins: String) {
            plugins.forEach {
                this.plugins += when {
                    it.contains(".") -> "id(\"$it\")"
                    it.contains("-") -> "`$it`"
                    else -> it
                }
            }
        }

        /** Appends a raw block to the generated script (settings or build). */
        fun script(scriptBlock: () -> String) {
            scriptBlocks += scriptBlock().trimIndent()
        }

        protected fun generatePluginsBlock(): List<String> =
            buildList {
                add("plugins {")
                plugins.forEach { add("    $it") }
                add("}")
            }

        protected fun generateScriptBlocks(): List<String> =
            scriptBlocks.toList()

    }

    @ExampleProjectDsl
    abstract class ProjectBuilder(
        protected val projectDir: File,
    ) : CommonBuilder() {

        /** The project `group`. */
        var group: String = EXAMPLE_PROJECT_GROUP_ID

        /** The project `description`. */
        var description: String = ""

        private var includeMavenCentral: Boolean = false
        private val sourceFiles = mutableListOf<SourceFile>()

        /** Writes a custom Java source under `src/[sourceSet]/java/[classPath]`. */
        fun javaSource(
            sourceSet: String = "main",
            classPath: String = "org/eazyportal/example/Example.java",
            contentBlock: () -> String = { EXAMPLE_JAVA_SOURCE },
        ) {
            sourceFiles += SourceFile("src/$sourceSet/java/$classPath", contentBlock().trimIndent())
        }

        /** Writes a custom Kotlin source under `src/[sourceSet]/kotlin/[classPath]`. */
        fun kotlinSource(
            sourceSet: String = "main",
            classPath: String = "org/eazyportal/example/Example.kt",
            contentBlock: () -> String = { EXAMPLE_KOTLIN_SOURCE },
        ) {
            sourceFiles += SourceFile("src/$sourceSet/kotlin/$classPath", contentBlock().trimIndent())
        }

        /** Includes the Maven Central repository. */
        fun useMavenCentral() {
            includeMavenCentral = true
        }

        internal fun build() {
            File(projectDir, "build.gradle.kts")
                .also { it.parentFile.mkdirs() }
                .writeText(generateBuildGradleKtsContent())

            sourceFiles.forEach { source ->
                File(projectDir, source.relativePath)
                    .also { it.parentFile.mkdirs() }
                    .writeText(source.content)
            }
        }

        private fun generateBuildGradleKtsContent(): String =
            buildList {
                addAll(generatePluginsBlock())

                add("group = \"$group\"")
                add("description = \"$description\"")

                if (includeMavenCentral) {
                    add(
                        """
                        repositories {
                            mavenCentral()
                        }
                        """.trimIndent(),
                    )
                }

                addAll(generateScriptBlocks())
            }.joinToString(separator = "\n\n", postfix = "\n")
    }

    @ExampleProjectDsl
    class SubprojectBuilder(projectDir: File) : ProjectBuilder(projectDir)

    @ExampleProjectDsl
    class RootProjectBuilder(projectDir: File) : ProjectBuilder(projectDir) {

        init {
            description = EXAMPLE_ROOT_PROJECT_DESCRIPTION
        }

    }

    @ExampleProjectDsl
    class SettingsBuilder(
        private val projectDir: File,
    ) : CommonBuilder() {

        /** The root project name (`settings.gradle.kts`). */
        var rootProjectName: String = EXAMPLE_ROOT_PROJECT_NAME

        internal fun build(subprojectNames: List<String>) {
            File(projectDir, "settings.gradle.kts")
                .writeText(generateSettingsGradleKtsContent(subprojectNames))
        }

        private fun generateSettingsGradleKtsContent(subprojectNames: List<String>): String =
            buildList {
                addAll(generatePluginsBlock())

                addAll(generateScriptBlocks())

                add("rootProject.name = \"$rootProjectName\"")

                addAll(subprojectNames.map { "include(\"$it\")" })
            }.joinToString(separator = "\n\n", postfix = "\n")
    }

    private data class SourceFile(
        val relativePath: String,
        val content: String,
    )

    companion object {
        /**
         * Configures a synthetic project under [workingDir] and writes the files immediately.
         *
         * @return [workingDir]/[rootProjectName] (default: `eazyportal-example`) where the project was written.
         */
        fun exampleProject(
            workingDir: File,
            configure: ExampleProjectBuilder.() -> Unit = {},
        ): File =
            ExampleProjectBuilder(workingDir)
                .apply(configure)
                .build()

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
