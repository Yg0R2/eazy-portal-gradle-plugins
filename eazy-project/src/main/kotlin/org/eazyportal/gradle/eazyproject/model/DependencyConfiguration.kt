package org.eazyportal.gradle.eazyproject.model

enum class DependencyConfiguration(
    private val configurationName: String,
) {

    API("api"),
    IMPLEMENTATION("implementation"),
    TEST_IMPLEMENTATION("testImplementation");

    override fun toString(): String =
        configurationName

}
