package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.ARTIFACT_ID
import org.eazyportal.gradle.internal.project.ExampleProjectFixtures.VERSION
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
    fun `a plain 'build' produces a usable jar and a sources jar, but no javadoc jar`(@TempDir projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-library-convention")
            .withMavenCentral()
            .withKotlinSource {
                """
                package org.eazyportal.example

                public fun greetings(): String = "Hello World!"
                """.trimIndent()
            }.build()

        runGradleTask(projectDir, "build", "-Pversion=$VERSION", withPluginClasspath = true)

        val jar = File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION.jar")
        assertThat(jar).exists()
        assertThat(classEntriesOf(jar)).contains("org/eazyportal/example/ExampleKt.class")

        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-sources.jar")).exists()
        assertThat(File(projectDir, "build/libs/$ARTIFACT_ID-$VERSION-javadoc.jar")).doesNotExist()
    }

    /** The `.class` entries in [jar] — proof the jar is a real, loadable artifact and not just a file that happens to exist. */
    private fun classEntriesOf(jar: File): List<String> =
        ZipFile(jar).use { zip -> zip.entries().asSequence().map { it.name }.filter { it.endsWith(".class") }.toList() }

}
