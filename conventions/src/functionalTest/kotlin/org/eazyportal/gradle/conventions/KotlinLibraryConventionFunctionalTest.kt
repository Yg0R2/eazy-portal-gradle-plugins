package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.GradleUtils.runGradleTask
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile

/**
 * Black-box coverage for `org.eazyportal.gradle.kotlin-library-convention` (design §4.4, TOOLS-63/§9.2, TOOLS-64):
 * running the single, unmodified `build` lifecycle task that a consumer would actually invoke — not the
 * narrower per-mechanism tasks the `integrationTest` suite drives — produces a main jar whose contents are
 * the compiled `explicitApi`-compliant sources, plus a sources jar and no Javadoc jar.
 */
class KotlinLibraryConventionFunctionalTest {

    @Test
    fun `a plain 'build' produces a usable jar and a sources jar, but no javadoc jar`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.kotlin-library-convention")

                useMavenCentral()

                kotlinSource {
                    """
                    package org.eazyportal.example

                    public fun greetings(): String = "Hello World!"
                    """.trimIndent()
                }
            }
        }

        runGradleTask(projectDir, "build", "-Pversion=$EXAMPLE_PROJECT_VERSION")

        val jar = File(projectDir, "build/libs/$EXAMPLE_ROOT_PROJECT_NAME-$EXAMPLE_PROJECT_VERSION.jar")
        assertThat(jar).exists()
        assertThat(classEntriesOf(jar)).contains("org/eazyportal/example/ExampleKt.class")

        assertThat(File(projectDir, "build/libs/$EXAMPLE_ROOT_PROJECT_NAME-$EXAMPLE_PROJECT_VERSION-sources.jar")).exists()
        assertThat(File(projectDir, "build/libs/$EXAMPLE_ROOT_PROJECT_NAME-$EXAMPLE_PROJECT_VERSION-javadoc.jar")).doesNotExist()
    }

    /** The `.class` entries in [jar] — proof the jar is a real, loadable artifact and not just a file that happens to exist. */
    private fun classEntriesOf(jar: File): List<String> =
        ZipFile(jar).use { zip -> zip.entries().asSequence().map { it.name }.filter { it.endsWith(".class") }.toList() }

}
