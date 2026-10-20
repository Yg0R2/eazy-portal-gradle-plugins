package org.eazyportal.gradle.conventions

import org.eazyportal.gradle.utils.gradle.GradleRunnerBuilder.Companion.gradleRunner
import org.eazyportal.gradle.utils.project.ExampleProjectBuilder.Companion.exampleProject
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_GROUP_ID
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_PROJECT_SNAPSHOT_VERSION
import org.eazyportal.gradle.utils.project.ExampleProjectFixtures.EXAMPLE_ROOT_PROJECT_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Black-box coverage for `org.eazyportal.gradle.java-platform-convention`: running the `publishToMavenLocal` task that a
 * consumer would actually invoke publishes a platform — a POM and Gradle module metadata that carry the
 * constraint, and no jar.
 */
class JavaPlatformConventionFunctionalTest {

    @Test
    fun `publishToMavenLocal publishes the POM and the module metadata of the platform, but no jar`(@TempDir workingDir: File) {
        val projectDir = exampleProject(workingDir) {
            rootProject {
                plugins("org.eazyportal.gradle.java-platform-convention")

                script {
                    """
                    dependencies {
                        constraints {
                            api("org.example:constrained:2.0")
                        }
                    }
                    """.trimIndent()
                }
            }
        }

        val repositoryDir = File(workingDir, "repo")

        gradleRunner(projectDir)
            .runGradleTask(
                "publishToMavenLocal",
                "-Pversion=$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
                "-Dmaven.repo.local=${repositoryDir.absolutePath}",
            )

        val artifactDir = File(
            repositoryDir,
            "${EXAMPLE_PROJECT_GROUP_ID.replace('.', '/')}/$EXAMPLE_ROOT_PROJECT_NAME/$EXAMPLE_PROJECT_SNAPSHOT_VERSION",
        )

        val publishedExtensions = artifactDir.listFiles().orEmpty().map { it.extension }
        assertThat(publishedExtensions).contains("pom", "module").doesNotContain("jar")

        assertThat(artifactDir.listFiles { it.extension == "module" }.orEmpty().single().readText())
            .contains(""""module": "constrained"""")
            .contains(""""requires": "2.0"""")
    }

}
