package org.eazyportal.gradle.utils.repository

import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_BOM_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_COMMON_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_GROUP_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_TEST_ARTIFACT_ID
import org.eazyportal.gradle.utils.repository.ExampleRepositoryFixtures.CORE_VERSION
import java.io.File

/**
 * Kotlin DSL for a synthetic Maven2-layout repository directory, materializing the minimal `pom` [+ `jar`]
 * files needed for a real Gradle dependency resolution against it to succeed.
 *
 * Only the parts that differ between tests need to be declared — everything else has a sensible default for a minimal Maven2-layout repository.
 * The files are written as soon as the configure block returns.
 *
 * ```
 * val repositoryDir = exampleRepository(workingDir) {
*    artifact {
 *         artifactId = "core-bom"
 *         packaging = "pom"
 *         version = "1.0.0"
 *         managedArtifactIds = listOf(
 *             "core-common",
 *             "core-test",
 *         )
 *     }
 *
 *     artifact {
 *         artifactId = "core-common"
 *         version = "1.0.0"
 *     }
 *
 *     artifact {
 *         artifactId = "core-test"
 *         version = "1.0.0"
 *     }
 * }
 * ```
 *
 * If the version is provided to the `exampleRepository()` function, it is used as the default version for all artifacts that do not explicitly declare a version.
 */
@ExampleRepositoryDsl
class ExampleRepositoryBuilder(
    workingDir: File,
    private val version: String = CORE_VERSION,
) {

    private val repositoryDir = workingDir
        .resolve("repository")
        .also { it.mkdirs() }

    private val artifacts = mutableListOf<ArtifactBuilder>()

    /** Declares EazyPortal artifacts. */
    fun eazyportalArtifacts() {
        artifact {
            artifactId = CORE_BOM_ARTIFACT_ID
            packaging = "pom"
            managedArtifactIds = listOf(
                CORE_COMMON_ARTIFACT_ID,
                CORE_TEST_ARTIFACT_ID,
            )
        }

        artifact {
            artifactId = CORE_COMMON_ARTIFACT_ID
        }

        artifact {
            artifactId = CORE_TEST_ARTIFACT_ID
        }
    }

    /**
     * Declares an artifact with pom packaging - by default - and also with the given [packaging].
     * [managedArtifactIds] — sharing the same [groupId]/[version] — are emitted as `<dependencyManagement>` entries,
     * e.g. for a BOM artifact (`packaging = "pom"`) managing the versions of a set of other artifacts.
     */
    fun artifact(configure: ArtifactBuilder.() -> Unit) {
        artifacts.add(ArtifactBuilder().apply(configure))
    }

    internal fun build(): File {
        artifacts.forEach { it.build(repositoryDir, version) }

        return repositoryDir
    }

    @ExampleRepositoryDsl
    class ArtifactBuilder {

        var artifactId: String = ""

        var groupId: String = CORE_GROUP_ID

        var version: String? = null

        var packaging: String = "jar"

        var managedArtifactIds: List<String> = emptyList()

        internal fun build(repositoryDir: File, version: String) {
            val artifactDir = File(repositoryDir, "${groupId.replace('.', '/')}/$artifactId/$version")
                .also { it.mkdirs() }

            // Built with plain concatenation, not a nested trimIndent(): mixing raw-string indentation levels leaves
            // stray leading whitespace before <?xml ...?> that XML parsers reject as "Content is not allowed in prolog".
            val dependencies = managedArtifactIds.joinToString("\n") {
                """
                <dependency>
                    <groupId>$groupId</groupId>
                    <artifactId>$it</artifactId>
                    <version>${this.version ?: version}</version>
                </dependency>
                """
            }

            File(artifactDir, "$artifactId-$version.pom").writeText(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>$groupId</groupId>
                    <artifactId>$artifactId</artifactId>
                    <version>$version</version>
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
                File(artifactDir, "$artifactId-$version.jar").writeBytes(
                    byteArrayOf(0x50, 0x4b, 0x05, 0x06) + ByteArray(18),
                )
            }
        }

    }

    companion object {
        /**
         * Configures a synthetic repository under [workingDir] and writes the files immediately.
         *
         * @return [workingDir]/repository where the artifacts were written.
         */
        fun exampleRepository(
            workingDir: File,
            version: String = CORE_VERSION,
            configure: ExampleRepositoryBuilder.() -> Unit = {},
        ): File =
            ExampleRepositoryBuilder(workingDir, version)
                .apply(configure)
                .build()
    }

}
