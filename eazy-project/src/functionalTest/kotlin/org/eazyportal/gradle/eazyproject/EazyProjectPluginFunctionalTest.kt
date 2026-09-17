package org.eazyportal.gradle.eazyproject

import org.eazyportal.gradle.conventions.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.conventions.project.ExampleProjectBuilder
import org.eazyportal.gradle.eazyproject.DefaultVersions.EXAMPLE_CORE_DEFAULT_VERSION
import org.eazyportal.gradle.eazyproject.EazyProjectPlugin.Companion.EAZY_PROJECT_DIAGNOSTICS_TASK_NAME
import org.eazyportal.gradle.eazyproject.model.ProjectType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.nio.file.Files

/**
 * Black-box coverage for `org.eazyportal.gradle.eazy-project` (design §5.5/§5.7, TOOLS-67 acceptance criteria):
 * real Gradle builds,
 * driven through TestKit's plugin-under-test classpath — which,
 * because `eazy-project` depends on `conventions` (the LINCHPIN edge, §5.5),
 * also carries every `org.eazyportal.gradle.*-convention` plugin —
 * proving the cascade actually takes effect rather than just asserting `hasPlugin(...)` in-process
 * (already covered by [org.eazyportal.gradle.eazyproject.configurer.ProjectConfigurerWiringTest] and [org.eazyportal.gradle.eazyproject.configurer.EazyProjectPluginTest]).
 */
class EazyProjectPluginFunctionalTest {

    @CsvSource(
        value = [
            "common,        org.eazyportal.gradle.kotlin-library-convention",
            "application,   org.eazyportal.gradle.kotlin-project-convention",
            "extras,        org.eazyportal.gradle.kotlin-project-convention",
        ]
    )
    @ParameterizedTest(name = "module ''{0}'' maps to {1}")
    fun `archetype to convention mapping is correct`(
        subprojectName: String,
        expectedConvention: String,
        @TempDir workingDir: File,
    ) {
        // `application` depends on every other layer sibling (ApplicationProjectConfigurer),
        // so those siblings must exist as projects — the dependency is a `project.project(":x")` reference,
        // not a real build of them.
        val siblings =
            if (subprojectName == "application") {
                listOf("common", "api", "persistence", "service", "client", "web")
            } else {
                emptyList()
            }

        val projectDir = buildExampleProject(workingDir, subprojectName, siblingProjectNames = siblings)

        val output = runGradleTask(projectDir, ":$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME")

        val expectedArchetype = ProjectType.fromProjectName(subprojectName).toString()
        val expectedSiblings = siblings.joinToString(", ", "[", "]") {
            ":$it (implementation)"
        }
        val expectedExampleCoreDependency = subprojectName
            .takeIf { expectedArchetype == it }
            ?.let { "eazyportal-core-$subprojectName (implementation), " }
            .orEmpty()
        assertThat(output).containsSubsequence(
            "> Task :$subprojectName:$EAZY_PROJECT_DIAGNOSTICS_TASK_NAME",
            "eazy-project diagnostics — :$subprojectName",
            "  archetype              : $expectedArchetype",
            "  convention             : [$expectedConvention]",
            "  siblings               : $expectedSiblings",
            "  eazyportal-core        : [${expectedExampleCoreDependency}eazyportal-core-test (testImplementation)]   (versions via the eazyportal-core BOM)",
            "  eazyPortalCoreVersion  : $EXAMPLE_CORE_DEFAULT_VERSION   (source: default (DefaultVersions))",
        )
    }

    @Test
    fun `applying to the root project throws`(@TempDir workingDir: File) {
        val projectDir = ExampleProjectBuilder(workingDir)
            .withRootProjectName("receiver")
            .withBuildPlugins("org.eazyportal.gradle.eazy-project")
            .build()

        val output = runFailingGradleTask(projectDir, "help")

        assertThat(output)
            .contains("org.eazyportal.gradle.eazy-project plugin must not be applied to the root project — it is only for subprojects.")
    }

    @Test
    fun `the transitively-applied convention takes effect, not just its id`(@TempDir workingDir: File) {
        val eazyPortalCoreRepo = File(workingDir, "eazyportal-core-repo")
            .also {
                Files.createDirectories(it.toPath())

                writeExampleCoreRepo(it)
            }

        val rootDir = buildExampleProject(
            projectDir = workingDir.resolve("receiver").also { Files.createDirectories(it.toPath()) },
            subprojectName = "common",
            eazyPortalCoreRepo = eazyPortalCoreRepo,
        ) {
            withKotlinSource(classPath = "org/eazyportal/example/Example.kt") {
                """
                package org.eazyportal.example

                // kotlin-library-convention (transitively applied by eazy-project) turns on explicitApi() —
                // an implicitly-public top-level declaration must fail to compile under it.
                fun greetings(): String = "Hello World!"
                """
            }
        }

        val output = runFailingGradleTask(rootDir, ":common:compileKotlin")

        assertThat(output).contains("Visibility must be specified in explicit API mode.")
    }

    @Test
    fun `a default module applies kotlin-library-convention by bare id and publishes`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(
            projectDir = workingDir,
            subprojectName = "extras",
            extraPluginIds = listOf("org.eazyportal.gradle.kotlin-library-convention"),
        )

        runGradleTask(projectDir, ":extras:publishToMavenLocal", "-Pversion=1.0.0-SNAPSHOT")
    }

    /**
     * A receiver: an empty root + `moduleName`'s subproject applying `eazy-project` [+ `extraPluginIds`],
     * plus an empty subproject per [siblingProjectNames] — needed only so a `project.project(":x")` sibling reference
     * (e.g. `application`'s, which depends on every other layer) resolves; they are never built themselves.
     * The root always carries `allprojects { repositories { mavenCentral() [+ eazyPortalCoreRepo] } }` — needed once
     * real compilation/publishing is exercised. [eazyPortalCoreRepo], when given, is the `file://`-published sample
     * (see [writeExampleCoreRepo]) so a real `compileClasspath` resolution of `org.eazyportal.gradle:eazyportal-core-*`
     * (added unconditionally by every [org.eazyportal.gradle.eazyproject.configurer.GradleProjectConfigurer]) succeeds —
     * those coordinates are design placeholders for a real internal registry, not anything mavenCentral() has.
     */
    private fun buildExampleProject(
        projectDir: File,
        subprojectName: String,
        extraPluginIds: List<String> = emptyList(),
        siblingProjectNames: List<String> = emptyList(),
        eazyPortalCoreRepo: File? = null,
        configureSubproject: ExampleProjectBuilder.() -> Unit = {},
    ): File =
        ExampleProjectBuilder(projectDir)
            .withRootProjectName("receiver")
            .withBuildScript {
                """
                allprojects {
                    repositories {
                        mavenCentral()
                        ${eazyPortalCoreRepo?.let { "maven { url = uri(\"${it.toURI()}\") }" }.orEmpty()}
                    }
                }
                """
            }.withSubproject(subprojectName) {
                withBuildPlugins("org.eazyportal.gradle.eazy-project", *extraPluginIds.toTypedArray())

                configureSubproject()
            }.apply {
                siblingProjectNames.forEach { withSubproject(it) }
            }.build()

    /**
     * Hand-written minimal `org.eazyportal.core:eazyportal-core-{bom,common,test}:[EXAMPLE_CORE_DEFAULT_VERSION]` artifacts in a Maven2-layout directory —
     * the smallest possible stand-in for the "file://-published sample" TOOLS-67's acceptance criteria calls for,
     * just enough for a real `compileClasspath` to resolve
     * (a `platform()`-only POM for the BOM, POM + an empty-but-valid jar for the two libraries actually placed on a configuration).
     */
    private fun writeExampleCoreRepo(repoDir: File) {
        // The BOM's own dependencyManagement is what pins the versionless eazyportal-core-* dependencies
        // GradleProjectConfigurer adds (design §5.6) — without it `platform(...)` imports no constraints at all.
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

        // Built with plain concatenation, not nested trimIndent()s: mixing raw-string indentation levels
        // leaves stray whitespace before <?xml ...?>, which XML parsers reject as "Content is not allowed in prolog".
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

}
