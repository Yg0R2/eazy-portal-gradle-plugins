package org.eazyportal.gradle.internal

import org.eazyportal.gradle.internal.GradleUtils.runFailingGradleTask
import org.eazyportal.gradle.internal.GradleUtils.runGradleTask
import org.eazyportal.gradle.internal.assertion.extension.taskDidRun
import org.eazyportal.gradle.internal.assertion.extension.taskDidNotRun
import org.eazyportal.gradle.internal.project.ExampleProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for the `test` / `functionalTest` / `integrationTest` suite wiring in `org.eazyportal.gradle.kotlin-project-convention` (design §7.4, TOOLS-64):
 * the three tiers run independently when invoked directly, and — under `check`, where they run together, ORDERED (`mustRunAfter`, not `dependsOn`) —
 * a failure in a lower tier aborts the default (no `--continue`) build before any higher tier starts.
 *
 * Each probe project has one trivial JUnit test per tier that fails only when `-PfailTestTier=<tier>` names it,
 * so a single build script drives every scenario below without touching the real `conventions`/`test` sources.
 */
class KotlinProjectConventionTestSuiteOrderingIntegrationTest {

    @Test
    fun `running 'test' alone executes only the unit tier`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runGradleTask(projectDir, "test", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("test")
            .taskDidNotRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `running 'functionalTest' alone executes only the functional tier`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runGradleTask(projectDir, "functionalTest", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("functionalTest")
            .taskDidNotRun("test")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `running 'integrationTest' alone executes only the integration tier`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runGradleTask(projectDir, "integrationTest", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("integrationTest")
            .taskDidNotRun("test")
            .taskDidNotRun("functionalTest")
    }

    @Test
    fun `a failing unit test aborts 'check' before the functional or integration test starts`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runFailingGradleTask(projectDir, "check", "-PfailTestTier=test", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("test")
            .taskDidNotRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `a failing functional test aborts 'check' before the integration test starts, after the unit test ran`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runFailingGradleTask(projectDir, "check", "-PfailTestTier=functionalTest", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("test")
            .taskDidRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `a failing integration test aborts 'check', after the unit and integration test ran`(@TempDir projectDir: File) {
        buildExampleProject(projectDir)

        val output = runFailingGradleTask(projectDir, "check", "-PfailTestTier=integrationTest", withPluginClasspath = true)

        assertThat(output)
            .taskDidRun("test")
            .taskDidRun("functionalTest")
            .taskDidRun("integrationTest")
    }

    /**
     * A minimal `org.eazyportal.gradle.kotlin-project-convention` project with one probe test per tier (`src/test`, `src/functionalTest`, `src/integrationTest`),
     * each failing only for the tier named by `-PfailTestTier`.
     *
     * No main sources — the tier-ordering wiring under test doesn't need any.
     */
    private fun buildExampleProject(projectDir: File) {
        ExampleProjectBuilder(projectDir)
            .withBuildPlugins("org.eazyportal.gradle.kotlin-project-convention")
            .withMavenCentral()
            .withBuildScript {
                """
                tasks.withType<Test>().configureEach {
                    systemProperty("failTestTier", providers.gradleProperty("failTestTier").getOrElse(""))
                }
                """.trimIndent()
            }.withKotlinSource("test") {
                createExampleKotlinTestSource("test")
            }.withKotlinSource("functionalTest") {
                createExampleKotlinTestSource("functionalTest")
            }.withKotlinSource("integrationTest") {
                createExampleKotlinTestSource("integrationTest")
            }.build()
    }

    companion object {
        /** Creates a one-test JUnit Jupiter class source that fails only when `failTestTier == sourceSet`. */
        private fun createExampleKotlinTestSource(sourceSet: String): String =
            """
                package org.eazyportal.example

                import org.junit.jupiter.api.Assertions.assertNotEquals
                import org.junit.jupiter.api.Test

                class Example${sourceSet.replaceFirstChar { it.uppercase() }} {

                    @Test
                    fun test() {
                        assertNotEquals("$sourceSet", System.getProperty("failTestTier"))
                    }

                }
            """.trimIndent()
    }

}
