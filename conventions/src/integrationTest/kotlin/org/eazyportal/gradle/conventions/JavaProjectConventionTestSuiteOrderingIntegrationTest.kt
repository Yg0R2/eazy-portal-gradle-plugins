package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.conventions.assertion.extension.taskDidRun
import org.eazyportal.gradle.conventions.assertion.extension.taskDidNotRun
import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TestKit coverage for the `test` / `functionalTest` / `integrationTest` suite wiring in `org.eazyportal.gradle.java-project-convention` (design §7.4, TOOLS-64):
 * the three tiers run independently when invoked directly, and — under `check`, where they run together, ORDERED (`mustRunAfter`, not `dependsOn`) —
 * a failure in a lower tier aborts the default (no `--continue`) build before any higher tier starts.
 *
 * Each probe project has one trivial JUnit test per tier that fails only when `-PfailTestTier=<tier>` names it,
 * so a single build script drives every scenario below without touching the real `conventions`/`test` sources.
 */
class JavaProjectConventionTestSuiteOrderingIntegrationTest {

    @Test
    fun `running 'test' alone executes only the unit tier`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runGradleTask("test")

        assertThat(output)
            .taskDidRun("test")
            .taskDidNotRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `running 'functionalTest' alone executes only the functional tier`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runGradleTask("functionalTest")

        assertThat(output)
            .taskDidRun("functionalTest")
            .taskDidNotRun("test")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `running 'integrationTest' alone executes only the integration tier`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runGradleTask("integrationTest")

        assertThat(output)
            .taskDidRun("integrationTest")
            .taskDidNotRun("test")
            .taskDidNotRun("functionalTest")
    }

    @Test
    fun `a failing unit test aborts 'check' before the functional or integration test starts`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("check", "-PfailTestTier=test")

        assertThat(output)
            .taskDidRun("test")
            .taskDidNotRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `a failing functional test aborts 'check' before the integration test starts, after the unit test ran`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("check", "-PfailTestTier=functionalTest")

        assertThat(output)
            .taskDidRun("test")
            .taskDidRun("functionalTest")
            .taskDidNotRun("integrationTest")
    }

    @Test
    fun `a failing integration test aborts 'check', after the unit and integration test ran`(@TempDir workingDir: File) {
        val projectDir = buildExampleProject(workingDir)

        val output = gradleRunner(projectDir)
            .runFailingGradleTask("check", "-PfailTestTier=integrationTest")

        assertThat(output)
            .taskDidRun("test")
            .taskDidRun("functionalTest")
            .taskDidRun("integrationTest")
    }

    /**
     * A minimal `org.eazyportal.gradle.java-project-convention` project with one probe test per tier (`src/test`, `src/functionalTest`, `src/integrationTest`),
     * each failing only for the tier named by `-PfailTestTier`.
     *
     * No main sources — the tier-ordering wiring under test doesn't need any.
     */
    private fun buildExampleProject(workingDir: File): File =
        exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.java-project-convention")

                useMavenCentral()

                script {
                    """
                    tasks.withType<Test>().configureEach {
                        systemProperty("failTestTier", providers.gradleProperty("failTestTier").getOrElse(""))
                    }
                    """.trimIndent()
                }


                javaSource("test") {
                    createExampleJavaTestSource("test")
                }
                javaSource("functionalTest") {
                    createExampleJavaTestSource("functionalTest")
                }
                javaSource("integrationTest") {
                    createExampleJavaTestSource("integrationTest")
                }
            }
        }

    companion object {
        /** Creates a one-test JUnit Jupiter class source that fails only when `failTestTier == sourceSet`. */
        private fun createExampleJavaTestSource(sourceSet: String): String =
            """
                package org.eazyportal.example;

                import org.junit.jupiter.api.Test;

                import static org.junit.jupiter.api.Assertions.assertNotEquals;

                class Example${sourceSet.replaceFirstChar { it.uppercase() }} {

                    @Test
                    public void test() {
                        assertNotEquals("$sourceSet", System.getProperty("failTestTier"));
                    }

                }
            """.trimIndent()
    }

}
