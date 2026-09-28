package org.eazyportal.gradle.utils.repository

object ExampleRepositoryFixtures {

    val CORE_BOM_ARTIFACT_ID = createCoreArtifactId("bom")

    val CORE_TEST_PROJECT_ARTIFACT_ID = createCoreArtifactId("test")

    const val CORE_GROUP_ID = "org.eazyportal.core"
    const val CORE_VERSION = "0.0.1"

    fun createCoreArtifactId(projectName: String): String =
        "eazyportal-core-$projectName"

}
