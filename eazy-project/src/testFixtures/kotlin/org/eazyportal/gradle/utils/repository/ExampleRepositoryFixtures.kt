package org.eazyportal.gradle.utils.repository

import org.eazyportal.gradle.eazyproject.model.Framework
import org.eazyportal.gradle.eazyproject.model.ProjectType

object ExampleRepositoryFixtures {

    val CORE_BOM_ARTIFACT_ID = createCoreArtifactId("bom", Framework.SPRING_BOOT)

    val CORE_TEST_PROJECT_ARTIFACT_ID = createCoreArtifactId("test")

    const val CORE_GROUP_ID = "org.eazyportal.core"
    const val CORE_VERSION = "0.0.1"

    /**
     * `eazyportal-[{framework}-]core-{projectName}` — the framework segment is present only for a
     * [ProjectType.isFrameworkSpecific] project (or `bom`); for any other project [framework] is ignored.
     *
     * @throws IllegalArgumentException if the project is framework-specific (or `bom`) and [framework] is `null`.
     */
    fun createCoreArtifactId(projectName: String, framework: Framework? = null): String {
        val projectType = ProjectType.fromProjectName(projectName)

        return if (projectType.isFrameworkSpecific || (projectName == "bom")) {
            if (framework == null) {
                throw IllegalArgumentException("Cannot create artifact id for framework-specific project type $projectType without a framework")
            }

            listOf("eazyportal", framework.toString(), "core", projectName)
        } else {
            listOf("eazyportal", "core", projectName)
        }.joinToString("-")
    }

    /**
     * The artifact id [projectType] depends on: framework-prefixed only for a [ProjectType.isFrameworkSpecific] type,
     * otherwise [framework] is ignored.
     *
     * @throws IllegalArgumentException if [projectType] is framework-specific and [framework] is `null`.
     */
    fun createCoreArtifactId(projectType: ProjectType, framework: Framework? = null): String =
        createCoreArtifactId(projectType.toString(), framework)

}
