package org.eazyportal.gradle.eazyproject.model

enum class DependencyConfiguration(
    private val configurationName: String,
) {

    API("api"),
    IMPLEMENTATION("implementation"),
    TEST_IMPLEMENTATION("testImplementation"),
    FUNCTIONAL_TEST_IMPLEMENTATION("functionalTestImplementation"),
    INTEGRATION_TEST_IMPLEMENTATION("integrationTestImplementation"),
    TEST_FIXTURES_IMPLEMENTATION("testFixturesImplementation");

    override fun toString(): String =
        configurationName

    companion object {
        /** Every non-production configuration that gets the eazyportal-core BOM and `eazyportal-core-test`: the three test tiers + test fixtures. */
        val TEST_CONFIGURATIONS: List<DependencyConfiguration> = listOf(
            TEST_IMPLEMENTATION,
            FUNCTIONAL_TEST_IMPLEMENTATION,
            INTEGRATION_TEST_IMPLEMENTATION,
            TEST_FIXTURES_IMPLEMENTATION,
        )
    }

}
